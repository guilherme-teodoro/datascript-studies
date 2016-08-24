(ns todo.core
  (:require [reagent.core :as r]
            [reagent.ratom :refer-macros [reaction]]
            [posh.reagent :refer [pull q posh!]]
            [datascript.core :as d]))

(def conn (d/create-conn))

(posh! conn)

(d/transact! conn [{:db/id -1
                    :profile/name "guilherme"
                    :profile/age 10}
                   {:profile/name "maiscedo"
                    :profile/age 40}
                   {:profile/name "teodoro"
                    :profile/age 40}])

(defn page []
  (let [profile-ids @(q '[:find [?todo ...]
                       :where [?todo :profile/name]] conn)]

    [:div [:h2 "Welcome to todo"]
     [:button {:on-click #(d/transact! conn [{:db/id -1
                                              :profile/name "diego"
                                              :profile/age 4}])} "+++++"]
     (doall
      (map
       (fn [n]
         ^{:key n} [:li (:profile/name @(pull conn '[*] n))])
       profile-ids))]))

(defn mount-root []
  (r/render [page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
