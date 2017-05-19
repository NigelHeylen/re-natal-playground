(ns playground.android.core
  (:require [om.next :as om :refer-macros [defui]]
            [re-natal.support :as sup]
            [playground.state :as state]
            [playground.elements :as el]))

(set! js/window.React (js/require "react"))
(def ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry ReactNative))

(def logo-img (js/require "./images/cljs.png"))

(defn alert [title]
  (.alert (.-Alert ReactNative) title))

(defui AppRoot
  static om/IQuery
  (query [this]
    '[:app/msg])
  Object
  (render [this]
    (let [{:keys [app/msg]} (om/props this)]
      (el/view {:style {:flexDirection "column" :margin 40 :alignItems "center"}}
               (el/text {:style {:fontSize 30 :fontWeight "100" :marginBottom 20 :textAlign "center"}} msg)
               (el/image {:source logo-img
                          :style  {:width 80 :height 80 :marginBottom 30}})
               (el/touchable-highlight {:style   {:backgroundColor "#999" :padding 10 :borderRadius 5}
                                        :onPress #(alert "HELLO!")}
                                       (el/text {:style {:color "white" :textAlign "center" :fontWeight "bold"}} "press me"))))))

(defonce RootNode (sup/root-node! 1))
(defonce app-root (om/factory RootNode))

(defn init []
  (om/add-root! state/reconciler AppRoot 1)
  (.registerComponent app-registry "Playground" (fn [] app-root)))