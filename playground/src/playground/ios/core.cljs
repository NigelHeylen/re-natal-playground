(ns playground.ios.core
  (:require [om.next :as om :refer-macros [defui]]
            [re-natal.support :as sup]
            [playground.state :as state]
            [playground.elements :as el]
            [cljs.reader :as reader]
            [goog.events :as events])
  (:import [goog.net XhrIo]
           goog.net.EventType
           [goog.events EventType]))
(def url "http://localhost:3500/")

(set! js/window.React (js/require "react"))
(def ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry ReactNative))

(def logo-img (js/require "./images/cljs.png"))

(defn alert [title]
  (.alert (.-Alert ReactNative) title))


(def ^:private meths
  {:get    "GET"
   :put    "PUT"
   :post   "POST"
   :delete "DELETE"})

(defn edn-xhr [{:keys [method url data on-complete]}]
  (let [xhr (XhrIo.)]
    (events/listen xhr goog.net.EventType.COMPLETE
                   (fn [e]
                     (on-complete (reader/read-string (.getResponseText xhr)))))
    (. xhr
       (send url (meths method) (when data (pr-str data))
             #js {"Content-Type" "application/edn"}))))

(defn class-item [class]
  (el/text {:style {:fontSize 20 :fontWeight "100" :marginBottom 20 :textAlign "center"}} (:class/title class)))

(defui AppRoot
  static om/IQuery
  (query [this]
    '[:app/msg :names :classes])
  Object
  (initLocalState [this] {:name ""})
  (componentWillMount [this]
    (edn-xhr
      {:method      :get
       :url         (str url "classes")
       :on-complete #(om/transact! this `[(state/put-classes {:value ~%})])}))
  (render [this]
    (let [{:keys [app/msg names classes]} (om/props this)
          {:keys [name]} (om/get-state this)]
      (el/view {:style {:flexDirection "column" :margin 40 :alignItems "center"}}
               (el/text {:style {:fontSize 30 :fontWeight "100" :marginBottom 20 :textAlign "center"}} msg)
               (el/image {:source logo-img
                          :style  {:width 80 :height 80 :marginBottom 30}})
               (el/touchable-highlight {:style   {:backgroundColor "#999" :padding 10 :borderRadius 5}
                                        :onPress (fn [_]
                                                   (om/update-state! this assoc :name "")
                                                   (om/transact! this `[(state/add-name {:value ~name})]))}
                                       (el/text {:style {:color "white" :textAlign "center" :fontWeight "bold"}} "press mee"))
               (el/text {:style {:fontSize 20 :fontWeight "100" :marginBottom 20 :textAlign "center"}} (str names))
               (el/text {:style {:fontSize 20 :fontWeight "100" :marginBottom 20 :textAlign "center"}} (count classes))
               (map class-item classes)
               (el/text-input {:style        {:fontSize 20 :backgroundColor "#999" :height 40 :width 100}
                               :value        name
                               :onChangeText #(om/update-state! this assoc :name %)})))))
;[(state/set-field {:value ~%
;:field :name})]

(defonce RootNode (sup/root-node! 1))
(defonce app-root (om/factory RootNode))

(defn init []
  (om/add-root! state/reconciler AppRoot 1)
  (.registerComponent app-registry "Playground" (fn [] app-root)))