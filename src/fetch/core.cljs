(ns fetch.core 
  (:require [goog.net.XhrIo :as xhr]
            [clojure.string :as string]
            [fetch.util :as util]
            [cljs.reader :as reader]
            [goog.events :as events]
            [goog.Uri.QueryData :as query-data]
            [goog.structs :as structs]))

(defn ->method [m]
  (string/upper-case (name m)))

(defn parse-route [route]
  (cond
    (string? route) ["GET" route]
    (vector? route) (let [[m u] route]
                      [(->method m) u])
    :else ["GET" route]))

(defn ->data [d]
  (let [cur (util/map->js d)]
    (query-data/createFromMap (structs/Map. cur))))

(defn ->callback [callback]
  (fn [req]
    (let [data (. req (getResponseText))]
      (callback data))))

(defn xhr [route content callback & [opts]]
  (let [req (new goog.net.XhrIo)
        [method uri] (parse-route route)
        data (->data content)
        callback (->callback callback)]
    (when callback
      (events/listen req goog.net.EventType/COMPLETE #(callback req)))
    (. req (send uri method data (when opts (util/map->js opts))))))
