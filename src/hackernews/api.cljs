(ns hackernews.api
  (:require 
   [goog.net.XhrIo :as xhr]
   [cljs.core.async :refer [chan close!]])
  (:require-macros
   [cljs.core.async.macros :refer [go alt!]]))

;; Send a GET request and return a chan
(defn GET [url]
  (let [ch (chan 1)]
    (xhr/send url
              (fn [e]
                (let [resp (-> e
                               .-target
                               .getResponseText)
                      val (js->clj (js/JSON.parse resp) :keywordize-keys true)]
                  (go (>! ch val) (close! ch)))))
    ch))


(defn top-stories []
  (GET "https://hacker-news.firebaseio.com/v0/topstories.json"))

(defn new-stories []
  (GET "https://hacker-news.firebaseio.com/v0/newstories.json"))

(defn get-item [itemId]
  (GET (str "https://hacker-news.firebaseio.com/v0/item/" itemId ".json")))
