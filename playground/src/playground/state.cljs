(ns playground.state
  (:require [om.next :as om]
            [re-natal.support :as sup]))


(enable-console-print!)

(defonce app-state (atom {:app/msg "Hello Clojure in iOS and Android!" :names [] :classes {}}))

(defmulti read om/dispatch)
(defmethod read :default
  [{:keys [state]} k _]
  (let [st @state]
    (if-let [[_ v] (find st k)]
      {:value v}
      {:value :not-found})))


(defmulti mutate om/dispatch)
(defmethod mutate :default [_ _ _])

(defmethod mutate `set-field [{:keys [state] :as env} key params]
    {:value {:keys [:value]}
     :action #(swap! state assoc (:field params) (:value params))})

(defmethod mutate `add-name [{:keys [state] :as env} key params]
  {:value {:keys [:names]}
   :action #(swap! state update :names conj (:value params))})

(defmethod mutate `put-classes [{:keys [state] :as env} key params]
  {:value  {:keys [:classes]}
   :action #(swap! state assoc :classes (:value params))})


(defonce reconciler
         (om/reconciler
           {:state        app-state
            :parser       (om/parser {:read read :mutate mutate})
            :root-render  sup/root-render
            :root-unmount sup/root-unmount}))