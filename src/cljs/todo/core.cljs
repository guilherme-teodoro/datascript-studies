(ns todo.core
  (:require [reagent.core :as r]
            [reagent.ratom :refer-macros [reaction]]
            [posh.reagent :refer [pull q posh!]]
            [datascript.core :as d]))

(def conn (d/create-conn))

(posh! conn)

(d/transact! conn [{:profile/name "guilherme"
                    :profile/age 10
                    :profile/sex :m}
                   {:profile/name "maiscedo"
                    :profile/age 40
                    :profile/sex :f}
                   {:profile/name "teodoro"
                    :profile/age 40
                    :profile/sex :m}])

(defn page []
  (let [value (r/atom nil)]
    (fn []
      (let [profile-ids @(q '[:find ?id ?name
                              :where [?id :profile/name ?name]] conn)]
        [:div [:h2 "Welcome to todo"]
         [:input {:type "text"
                  :value @value
                  :on-change (fn [e]
                               (reset! value (-> e .-target .-value)))}]
         [:button {:on-click #(d/transact! conn [{:profile/name @value
                                                  :profile/age 4
                                                  :profile/sex :m}])}
          "Adicionar"]
         (doall
          (map
           (fn [[id name]]
             ^{:key id} [:li (str name " - "
                                  (:profile/age @(pull conn '[:profile/age] id)))])
           profile-ids))]))))


(defn mount-root []
  (r/render [page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
