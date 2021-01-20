(ns paintings.display
  (:require [hiccup.core :as hiccup]))


(defn create-image-element [{:keys [object-number width height url]}]
  [:div {:class "photocard"}
   [:img {:src url}]])


(defn convert-to-hiccup [data]
  (let [images  ( map create-image-element data)]
    [:html
     [:head
      [:meta {:charset "utf-8"}]
      [:title " Most popular paintings from the Rijksmuseum "]
      [:link {:href "/css/styles.css", :rel "stylesheet"}]]
     [:body
      [:div {:class "scene"}
       [:div {:class "roll-camera"}
        [:div {:class "move-camera"}
         [:div {:class "wallpaper"}]
         [:div {:class "shelf top"}
          [:div {:class "face top"}]
          [:div {:class "face front"}
           (take 3 images)]
          [:div {:class "face back"}]
          [:dic {:class "face left"}]
          [:div {:class= "face bottom"}]]]]]]]))


(defn generate-html [data]
  (-> data
      (convert-to-hiccup)
      (hiccup/html)))