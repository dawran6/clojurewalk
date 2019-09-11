(ns admin
  (:require
   [buddy.hashers :as hashers]
   [coast]
   [components :refer [container submit input link-to table thead tbody th tr td button-to]]))

(defn sign-in
  [request]
  (container
   {:mw 6}
   [:h1 "Admin"]
   (when-let [error (:error/message request)]
     [:div error])
   (coast/form-for
    ::create
    (input {:type "email" :name "member/email"})
    (input {:type "password" :name "member/password"})
    (submit "Submit"))))

(defn create-session
  [request]
  (let [email           (get-in request [:params :member/email])
        member          (coast/find-by :member {:email email})
        [valid? errors] (-> (:params request)
                            (select-keys [:member/email :member/password])
                            (coast/validate [[:email [:member/email]]
                                             [:required [:member/email :member/password]]])
                            (get :member/password)
                            (hashers/check (:member/password member))
                            (coast/rescue))]
    (if (or (some? errors) (false? valid?))
      (sign-in (merge request {:error/message "Invalid email or password"}))
      (-> (coast/redirect-to ::dashboard)
          (assoc :session {:member/email email})))))

(defn delete-session
  [request]
  (-> (coast/redirect-to ::sign-in)
      (assoc :session nil)))

(defn dashboard
  [request]
  (let [video    (coast/q '[:select * :from video])
        maillist (coast/q '[:select * :from maillist])
        series   (coast/q '[:select * :from series])
        post     (coast/q '[:select * :from post])]
    (container
     {:mw 9}
     [:h1 "Dashboard"]

     [:div.mv3
      (coast/form-for ::delete-session
                      (submit "Sign out"))]

     ;; series
     [:h2 [:a.link.dim.blue {:href (coast/url-for :series/index)} "Series"]]
     (link-to (coast/url-for :series/build) "New series")

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
       (for [row series]
         (tr
          (td (:series/id row))
          (td (:series/updated-at row))
          (td (:series/slug row))
          (td (:series/created-at row))
          (td (:series/title row))
          (td
           (link-to (coast/url-for :series/view row) "View"))
          (td
           (link-to (coast/url-for :series/edit row) "Edit"))
          (td
           (button-to (coast/action-for ::delete row) {:data-confirm "Are you sure?"} "Delete"))))))


     ;; video


     [:h2 [:a.link.dim.blue {:href (coast/url-for :video/index)} "Video"]]
     (link-to (coast/url-for :video/build) "New video")

     (table
      (thead
       (tr
        (th "id")
        (th "youtubeid")
        (th "updated-at")
        (th "created-at")
        (th "title")
        (th "series")
        (th "")
        (th "")
        (th "")))
      (tbody
       (for [row video]
         (tr
          (td (:video/id row))
          (td (:video/youtubeid row))
          (td (:video/updated-at row))
          (td (:video/created-at row))
          (td (:video/title row))
          (td (:video/series row))
          (td
           (link-to (coast/url-for :video/view row) "View"))
          (td
           (link-to (coast/url-for :video/edit row) "Edit"))
          (td
           (button-to (coast/action-for ::delete row) {:data-confirm "Are you sure?"} "Delete"))))))

     ;; maillist
     [:h2 [:a.link.dim.blue {:href (coast/url-for :maillist/index)} "Maillist"]]

     (link-to (coast/url-for :maillist/build) "New maillist")

     (table
      (thead
       (tr
        (th "id")
        (th "display-name")
        (th "email")
        (th "updated-at")
        (th "created-at")
        (th "")
        (th "")
        (th "")))
      (tbody
       (for [row maillist]
         (tr
          (td (:maillist/id row))
          (td (:maillist/display-name row))
          (td (:maillist/email row))
          (td (:maillist/updated-at row))
          (td (:maillist/created-at row))
          (td
           (link-to (coast/url-for :maillist/view row) "View"))
          (td
           (link-to (coast/url-for :maillist/edit row) "Edit"))
          (td
           (button-to (coast/action-for :maillist/delete row) {:data-confirm "Are you sure?"} "Delete"))))))

     ;; post
     [:h2 [:a.link.dim.blue {:href (coast/url-for :post/index)} "Post"]]
     (link-to (coast/url-for :post/build) "New post")

     (table
      (thead
       (tr
        (th "id")
        (th "member")
        (th "published-at")
        (th "updated-at")
        (th "slug")
        (th "created-at")
        (th "title")
        (th "")
        (th "")
        (th "")))
      (tbody
       (for [row post]
         (tr
          (td (:post/id row))
          (td (:post/member row))
          (td (:post/published-at row))
          (td (:post/updated-at row))
          (td (:post/slug row))
          (td (:post/created-at row))
          (td (:post/title row))
          (td
           (link-to (coast/url-for :post/view row) "View"))
          (td
           (link-to (coast/url-for :post/edit row) "Edit"))
          (td
           (button-to (coast/action-for ::delete row) {:data-confirm "Are you sure?"} "Delete")))))))))

(comment
  ;; sign up a admin user
  (-> #:member{:email    "admin@email.com"
               :password "password"}
      (update :member/password hashers/derive)
      (coast/insert)))
