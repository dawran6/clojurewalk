(ns series
  (:require [coast]
            [components :refer [container tc link-to table thead tbody td th tr button-to text-muted mr2 dl dd dt submit input label]]))

(defn index [request]
  (let [rows (coast/q '[:select *
                        :from series
                        :order id
                        :limit 10])]
    (container {:mw 8}
               (when (not (empty? rows))
                 (link-to (coast/url-for ::build) "New series"))

               (when (empty? rows)
                 (tc
                  (link-to (coast/url-for ::build) "New series")))

               (when (not (empty? rows))
                 (table
                  (thead
                   (tr
                    (th "id")
                    (th "updated-at")
                    (th "slug")
                    (th "created-at")
                    (th "title")
                    (th "")
                    (th "")
                    (th "")))
                  (tbody
                   (for [row rows]
                     (tr
                      (td (:series/id row))
                      (td (:series/updated-at row))
                      (td (:series/slug row))
                      (td (:series/created-at row))
                      (td (:series/title row))
                      (td
                       (link-to (coast/url-for ::view row) "View"))
                      (td
                       (link-to (coast/url-for ::edit row) "Edit"))
                      (td
                       (button-to (coast/action-for ::delete row) {:data-confirm "Are you sure?"} "Delete"))))))))))

(defn view [request]
  (let [id     (-> request :params :series-id)
        series (coast/fetch :series id)]
    (container {:mw 8}
               (dl
                (dt "slug")
                (dd (:series/slug series))

                (dt "title")
                (dd (:series/title series)))
               (mr2
                (link-to (coast/url-for ::index) "List"))
               (mr2
                (link-to (coast/url-for ::edit {::id id}) "Edit"))
               (mr2
                (button-to (coast/action-for ::delete {::id id}) {:data-confirm "Are you sure?"} "Delete")))))

(defn errors [m]
  [:div {:class "bg-red white pa2 mb4 br1"}
   [:h2 {:class "f4 f-subheadline"} "Errors Detected"]
   [:dl
    (for [[k v] m]
      [:div {:class "mb3"}
       (dt (str k))
       (dd v)])]])

(defn build [request]
  (container {:mw 6}
             (when (some? (:errors request))
               (errors (:errors request)))

             (coast/form-for ::create
                             (label {:for "series/slug"} "slug")
                             (input {:type "text" :name "series/slug" :value (-> request :params :series/slug)})

                             (label {:for "series/title"} "title")
                             (input {:type "text" :name "series/title" :value (-> request :params :series/title)})

                             (link-to (coast/url-for ::index) "Cancel")
                             (submit "New series"))))

(defn create [request]
  (let [[_ errors] (-> (coast/validate (:params request) [[:required [:series/slug :series/title]]])
                       (select-keys [:series/slug :series/title])
                       (coast/insert)
                       (coast/rescue))]
    (if (nil? errors)
      (coast/redirect-to ::index)
      (build (merge request errors)))))

(defn edit [request]
  (let [series (coast/fetch :series (-> request :params :series-id))]
    (container {:mw 6}
               (when (some? (:errors request))
                 (errors (:errors request)))

               (coast/form-for ::change series
                               (label {:for "series/slug"} "slug")
                               (input {:type "text" :name "series/slug" :value (:series/slug series)})

                               (label {:for "series/title"} "title")
                               (input {:type "text" :name "series/title" :value (:series/title series)})

                               (link-to (coast/url-for ::index) "Cancel")
                               (submit "Update series")))))

(defn change [request]
  (let [series     (coast/fetch :series (-> request :params :series-id))
        [_ errors] (-> (select-keys series [:series/id])
                       (merge (:params request))
                       (coast/validate [[:required [:series/id :series/slug :series/title]]])
                       (select-keys [:series/id :series/slug :series/title])
                       (coast/update)
                       (coast/rescue))]
    (if (nil? errors)
      (coast/redirect-to ::index)
      (edit (merge request errors)))))

(defn delete [request]
  (let [[_ errors] (-> (coast/fetch :series (-> request :params :series-id))
                       (coast/delete)
                       (coast/rescue))]
    (if (nil? errors)
      (coast/redirect-to ::index)
      (-> (coast/redirect-to ::index)
          (coast/flash "Something went wrong!")))))
