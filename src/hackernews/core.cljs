(ns ^:figwheel-always hackernews.core
    (:require 
     [hackernews.api :as api]
     [om.core :as om :include-macros true]
     [om.dom :as dom :include-macros true])
    (:require-macros
     [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

(def LIST_COUNT 25)

;; define your app data so that it doesn't get over-written on reload
(defonce app-state 
  (atom {:text "Hello world!"
         :list []}))

(defn loading-view [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil "Loading......" data))))

(defn story-view [id owner]
  (reify
    om/IInitState
    (init-state [this]
      (println "story init-state" id)
      {:story #{}})

    om/IWillMount
    (will-mount [this]
      (go
        (let [story (<! (api/get-item id))]
          (om/set-state! owner :story story))))

    om/IRender
    (render [this]
      (let [story (om/get-state owner :story)
            loading? (= (count (keys story)) 0)]
        (if loading?
          (om/build loading-view id)
          (dom/div nil 
            (dom/a #js{:href (:url story)} (:title story))
            (dom/span nil (js/Date (:time story)))
            (dom/span nil " score: " (:score story))
            (dom/span nil " comments: "( :descendants story))))))))

(defn root-view [data owner]
  (reify 
    om/IWillMount
    (will-mount [_]
      (if (= (count (om/get-state owner :list)) 0)
        (go
          (let [stories (<! (api/top-stories))]
            (om/set-state! owner :list (take LIST_COUNT stories))))))

    om/IRender
    (render [this]
      (let [stories (om/get-state owner :list)]
        (if (= (count stories) 0)
          (om/build loading-view "News IDs")
          (dom/div nil 
            (om/build-all story-view stories)))))))

(om/root root-view app-state
  {:target (. js/document (getElementById "app"))})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
)

