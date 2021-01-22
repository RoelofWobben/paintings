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
   [:div {:class "face left"}]
   [:div {:class= "face bottom"}]])


(defn display-middle1 [images]
  [:div {:class "shelf middle1"}
   [:div {:class "face top"}]
   [:div {:class "face front"}
    (second images)]
   [:div {:class "face back"}]
   [:div {:class "face left"}]
   [:div {:class= "face bottom"}]])

(defn display-middle2 [images]
  [:div {:class "shelf middle2"}
   [:div {:class "face top"}]
   [:div {:class "face front"}
    (nth images 2)]
   [:div {:class "face back"}]
   [:div {:class "face left"}]
   [:div {:class= "face bottom"}]])


(defn display-bottom [images page-number]
  [:div {:class "shelf bottom"}
   [:div {:class "face top"}]
   [:div {:class "face front"}
    [:div {:class "photocard"}
     (when (> page-number 1)
       [:a {:href (str "/?pg=" (inc page-number))} [:i {:class "fas fa-chevron-left fa-10x"}]])]
    (nth images 3)
    [:div {:class "photocard"}
     (when (< page-number 1000)
       [:a {:href (str "/?pg=" (dec page-number))} [:i {:class "fas fa-chevron-right fa-10x"}]])]]
   [:div {:class "face back"}]
   [:div {:class "face left"}]
   [:div {:class= "face bottom"}]])



(defn display-body [data page-number]
  (let [images  (partition-all 3 (map create-image-element data))]
    [:body
     [:div {:class "scene"}
      [:div {:class "roll-camera"}
       [:div {:class "move-camera"}
        [:div {:class "wallpaper"}]
        (display-top-shelf images)
        (display-middle1 images)
        (display-middle2 images)
        (display-bottom images page-number)]]]]))

(defn convert-to-hiccup [data page-number]
  [:html
   [:head
    [:meta {:charset "utf-8"}]
    [:title " Most popular paintings from the Rijksmuseum "]
    [:link {:href "/css/styles.css", :rel "stylesheet"}]
    [:script {:src "https://kit.fontawesome.com/6f1527f147.js", :crossorigin " anonymous"}]]
   (display-body data page-number)])


(defn generate-html [data page-number]
  (-> data
      (convert-to-hiccup page-number)
      (hiccup/html)))


