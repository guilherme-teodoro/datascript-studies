(ns todo.core
  (:require [reagent.core :as r]
            [reagent.ratom :refer-macros [reaction]]
            [posh.reagent :refer [pull q posh!] :as p]
            [datascript.core :as d]))

(def schema {:story/id {:db.unique :db.unique/identity}
             :task/story {:db.valueType :db.type/long}})

(def conn (d/create-conn))

(posh! conn)

(d/transact! conn [{:story/id 1
                    :story/title "Holerite"}
                   {:story/id 2
                    :story/title "Sistema de Ponto"}
                   {:task/title "Selecao magica"
                    :task/done? false
                    :task/story 1}
                   {:task/title "Reconhecimento magico"
                    :task/done? false
                    :task/story 1}
                   {:task/title "Ponto via nfc"
                    :task/done? true
                    :task/story 2}
                   {:task/title "Ponto via nfc"
                    :task/done? false
                    :task/story 2}
                   {:task/title "Manda pros caras"
                    :task/done? false
                    :task/story 1}])

(defn add-task [stories]
  (let [value (r/atom nil)
        story (r/atom nil)]
    (fn [stories]
      [:form {:on-submit (fn [e]
                           (.preventDefault e)
                           (d/transact! conn [{:task/title @value
                                               :task/done? false
                                               :task/story @story}]))}
       [:h3 "adicionar de task"]
       [:label
        [:input {:type "text"
                 :value @value
                 :on-change #(reset! value (-> % .-target .-value))}]]

       [:br]
       (doall
        (for [s stories]
          ^{:key s}
          [:label
           [:input {:type "radio"
                    :checked (= @story s)
                    :on-change #(reset! story s)}]
           (:story/title @(pull conn '[:story/title] s))]))
       [:br]
       [:label
        [:button {:type "submit"} "Adicionar"]]])))

(defn page []
  (let [stories @(q '[:find [?id ...]
                      :where
                      [?id :story/id]] conn)]
    [:div
     [:div
      [:h1 "Tasks manheirinhas"]
      [:hr]
      (doall (for [story stories]
               ^{:key story}
               [:div
                [:h2 (:story/title @(pull conn '[:story/title] story))
                 (str " (" (count @(q '[:find [?task ...]
                                        :in $ ?story
                                        :where
                                        [?task :task/story ?story]
                                        [?task :task/done? true]] conn story)) ")")]
                [:h3 ]
                [:ul
                 (doall
                  (for [task @(q '[:find [?task ...]
                                   :in $ ?story
                                   :where
                                   [?c    :story/id ?story]
                                   [?task :task/story ?c]] conn story)]
                    ^{:key task}
                    [:li
                     [:input {:checked (:task/done? @(pull conn '[:task/done?] task))
                              :type "checkbox"
                              :on-change #(p/transact! conn [[:db/add task :task/done? (not (:task/done? @(pull conn '[:task/done?] task)))]])}]
                     [:span (:task/title @(pull conn '[:task/title] task))]]))]]))]
     [add-task stories]]))

(defn mount-root []
  (r/render [page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
