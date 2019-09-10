(ns routes
  (:require [coast]
            [layouts]
            [middleware]
            [admin]))

(def routes
  (coast/routes

    (coast/site
      (coast/with-layout layouts/doc-layout
        [:get "/read/:post-slug" :site.article/reader])

      (coast/with-layout layouts/layout
        ;; home
        [:get "/" :site.home/index]

        ;; videos
        [:get "/watch" :site.video/index]
        [:get "/watch/:youtubeid" :site.video/player]

        ;; articles
        [:get "/read" :site.article/index]

        ;; about
        [:get "/about" :site.about/index]

        ;; subscribe-to-newsletter
        [:post "/subscribe-to-newsletter" :site.about/subscribe]

        ;; admin
        [:get "/admin/sign-in" :admin/sign-in]
        [:post "/admin/sign-in" :admin/create-session]
        (coast/with
          middleware/auth middleware/current-member
          [:resource :video]
          [:resource :maillist]
          [:resource :series]
          [:resource :post]
          [:post "/posts/preview" :post/preview]
          [:get "/admin" :admin/dashboard]
          [:post "/admin/sign-out" :admin/delete-session])))

    (coast/api
      (coast/with-prefix "/api"
        [:get "/" :api.home/index]))))
