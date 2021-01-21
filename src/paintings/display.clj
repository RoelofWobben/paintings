(ns paintings.display
  (:require [hiccup.core :as hiccup]))

(defn create-image-element [{:keys [object-number width height url]}]
  [:div {:class "photocard"}
   [:img {:src url}]])

(defn display-top-shelf [images]
  [:div {:class "shelf top"}
   [:div {:class "face top"}]
   [:div {:class "face front"}
    (first images)]
   [:div {:class "face back"}]
   [:dic {:class "face left"}]
   [:div {:class= "face bottom"}]])


(defn display-middle1 [images]
  [:div {:class "shelf middle1"}
   [:div {:class "face top"}]
   [:div {:class "face front"}
    (second images)]
   [:div {:class "face back"}]
   [:dic {:class "face left"}]
   [:div {:class= "face bottom"}]])

(defn display-middle2 [images]
  [:div {:class "shelf middle1"}
   [:div {:class "face top"}]
   [:div {:class "face front"}
    (nth images 3)]
   [:div {:class "face back"}]
   [:dic {:class "face left"}]
   [:div {:class= "face bottom"}]])


(defn display-bottom [images]
  [:div {:class "shelf middle1"}
   [:div {:class "face top"}]
   [:div {:class "face front"}
    (nth images 4)]
   [:div {:class "face back"}]
   [:dic {:class "face left"}]
   [:div {:class= "face bottom"}]])


(defn display-body [data]
  (let [images  (partition 3 (map create-image-element data))]
    [:body
     [:div {:class "scene"}
      [:div {:class "roll-camera"}
       [:div {:class "move-camera"}
        [:div {:class "wallpaper"}]
        (display-top-shelf images)
        (display-middle1 images)
        (display-middle2 images)
        (display-bottom images)]]]]))

(defn convert-to-hiccup [data]
  [:html
   [:head
    [:meta {:charset "utf-8"}]
    [:title " Most popular paintings from the Rijksmuseum "]
    [:link {:href "/css/styles.css", :rel "stylesheet"}]]
   (display-body data)])


(defn generate-html [data]
  (-> data
      (convert-to-hiccup)
      (hiccup/html)))

