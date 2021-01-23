(ns paintings.core
  (:require [cheshire.core :as json]
            [clj-http.client :as client]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [paintings.display :as display]
            [ring.adapter.jetty]
            [ring.middleware.params :refer [wrap-params]])
  (:import [java.util Map]
           [java.util.concurrent ConcurrentHashMap]
           [java.util.function Function]))


(defn sort-by-name [col]
  (sort-by :name col))

(defn prepare-tile-image-data
  [{:keys [width height tiles]}]
  {:width width
   :height height
   :url (get-in tiles [0 :url])})

(defn fetch-smallest-tile
  "Calls the museum API to get the tiles for a given object number.
  Then returns the smallest tile image in the form of a map with the keys :width, :height, and :url"
  [object-number]
  (some-> (client/get (str "https://www.rijksmuseum.nl/api/nl/collection/" object-number "/tiles")
                      {:query-params {:key "14OGzuak"
                                      :format "json"}
                       :throw-exceptions false})
          (:body)
          (json/parse-string true)
          (:levels)
          (sort-by-name)
          (last)
          (prepare-tile-image-data)))

(defn assoc-image [{:keys [objectNumber webImage]}]
  (-> (or (fetch-smallest-tile objectNumber)
          webImage)
      (select-keys [:width :height :url])
      (merge {:object-number objectNumber})))

(defn take-data [api-data]
  (->> api-data
       (pmap assoc-image)
       doall))

(defn display-data [page]
  (-> (client/get "https://www.rijksmuseum.nl/api/nl/collection"
                  {:query-params {:key "14OGzuak"
                                  :format "json"
                                  :p page
                                  :type "schilderij"
                                  :toppieces "True"}})
      :body
      (json/parse-string true)
      :artObjects
      (take-data)))

;; The cache for display data values is a Java ConcurrentHashMap
(def display-data-cache (ConcurrentHashMap.))

(defn memo-display-data
  [page]
  ;; .computeIfAbsent is a method on Java's Map interface which will get a stored value for a given key
  ;; or compute, store, and return it using some function. In case of ConcurrentHashMap, any retrieval
  ;; operations for a key will wait if there is currently a compute for it in progress.
  (.computeIfAbsent
    ^Map display-data-cache
    page
    (reify Function                                         ;; `reify` is a macro that lets us implement Java interfaces, in this case "java.util.function.Function".
      (apply [_ p] (display-data p)))))


(defn extract-page-number [request]
  (Long/parseLong (get-in request [:query-params "pg"] "1")))

(defn home-handler
  [request]
  (let [page (extract-page-number request)]
    ;; pre-load next page
    (when (< page 478)
      (future (memo-display-data (inc page))))
    ;; pre-load previous page
    (when (> page 1)
      (future (memo-display-data (dec page))))
    (-> (memo-display-data page)
        (display/generate-html page))))

(defroutes app
  (GET "/" request (home-handler request))
  (route/resources "/")
  (GET "/favicon.ico" [] ""))

(defonce server (atom nil))

(defn stop-server []
  (when @server
    (.stop @server)
    (reset! server nil)))

(defn start-server []
  (stop-server)
  (reset! server
          (ring.adapter.jetty/run-jetty
           (wrap-params #'app)
           {:port 8080, :join? false})))

