(ns paintings.core
  (:require [cheshire.core :as json]
            [clj-http.client :as client]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [paintings.display :as display]
            [clojure.core.memoize :as memo]
            [ring.adapter.jetty]
            [ring.middleware.params :refer [wrap-params]]))


(defn image-url-size [image]
  (let [data (select-keys image [:width :height])
        url (get-in image [:tiles 0 :url])]
    (assoc data :url url)))

(defn take-image-data [image-data object-number]
  (->> image-data
       (sort-by :name)
       (last)
       (image-url-size)
       (merge {:object-number object-number})))

(defn assoc-image [object-number]
  (-> (client/get (str "https://www.rijksmuseum.nl/api/nl/collection/" object-number "/tiles")
                  {:query-params {:key "14OGzuak"
                                  :format "json"}})
      (:body)
      (json/parse-string true)
      (:levels)
      (take-image-data object-number)))

(defn take-data [api-data]
  (->> api-data
       (map :objectNumber)
       (map assoc-image)))

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

(def memo-display-data
  (memo/ttl display-data :ttl/threshold (* 60 60 1000)))    ;; memoize display-data result for 60 minutes (in milliseconds)

(defn extract-page-number [request]
  (Long/parseLong (get-in request [:query-params "pg"] "1")))

(defn home-handler
  [request]
  (let [page (extract-page-number request)]
    (future (memo-display-data (inc page))
            (-> (memo-display-data page)
                (display/generate-html page)))))

(defroutes app
  (GET "/" [request] home-handler request)
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

