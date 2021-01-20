(ns paintings.core
  (:require [cheshire.core :as json]
            [clj-http.client :as client]
            [compojure.core :refer :all]
            [ring.adapter.jetty]))


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

(defn display-data []
  (-> (client/get "https://www.rijksmuseum.nl/api/nl/collection"
                  {:query-params {:key "14OGzuak"
                                  :format "json"
                                  :type "schilderij"
                                  :toppieces "True"}})
      :body
      (json/parse-string true)
      :artObjects
      (take-data)))

(defroutes app
  (GET "/" [] (display-data)))

(defonce server (atom nil))

(defn stop-server []
  (when @server
    (.stop @server)
    (reset! server nil)))

(defn start-server []
  (stop-server)
  (reset! server
          (ring.adapter.jetty/run-jetty
            #'app {:port 9000, :join? false})))

