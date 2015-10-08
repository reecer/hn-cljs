(ns ^:figwheel-always hackernews.core
    (:require 
     [hackernews.api :as api]
     [om.core :as om :include-macros true]
     [om.dom :as dom :include-macros true])
    (:require-macros
     [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload
(defonce app-state 
  (atom {:text "Hello world!"
         :list []}))

(defn story-view [id owner]
  (reify
    om/IInitState
    (init-state [this]
      (println "story init-state" id)
      {:story #{}})

    om/IWillMount
    (will-mount [this]
      (println "mounting" id)
      (go
        (let [story (<! (api/get-item id))]
          (om/set-state! owner :story story))))

    om/IRenderState
    (render-state [this state]
      (let [story (om/get-state owner :story)]
        (dom/div nil (:title story) " - " (:url story))))))



(defn root-view [data owner]
  (reify 
    om/IDidMount
    (did-mount [_]
      (println "did mount")
      (if (= (count (om/get-state owner :list)) 0)
        (go
          (let [stories (<! (api/top-stories))]
            (om/set-state! owner :list (take 10 stories))))))

    om/IRender
    (render [this]
      (let [stories (om/get-state owner :list)]
        (println "root render" (count stories))
        (if (= (count stories) 0)
          (dom/div nil "Loading..." stories)
          (dom/div nil 
            (om/build-all story-view stories)))))))

(om/root root-view app-state
  {:target (. js/document (getElementById "app"))})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
)

