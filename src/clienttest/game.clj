(ns clienttest.game
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [clienttest.gamestats :as gamestats]
            [clj-http.client :as client])
  (:import
    (java.io PrintWriter InputStreamReader BufferedReader)
    (java.net Socket)))

(def moo (atom {:a 1}))

(defn convertfromstring
  [content]
  )

(def mock-state
  '({:role "player" :type "entity" :color 255 :x 20 :y 100 :velX 6 :velY 6}
     {:role "ball" :type "object" :color 255 :x 200 :y 200 :velX 5 :velY 5}
     {:role "enemy" :type "entity" :color 255 :x 760 :y 200 :velX 6 :velY 6}
     {:role "score" :type "logic" :player1 0 :player2 0}))


(defn writescore
  [state]
  (let [player1score (:player1 state)
        player2score (:player2 state)
        player1 (if gamestats/aipower
                  "Nadal"
                  "Player")]
    (q/text (str player1 "   " player1score "        |        " player2score "   Mr AI") 350 50)
    )
  )

(defn writeballspeed
  [state]
  (let [velX (:velX state)
        velY (:velY state)
        rawspeed (Math/sqrt (+ (* velX velX) (* velY velY)))
        speed (int (Math/floor rawspeed))]
    (q/text (str "speed:  " speed) 400 90)
    ;(q/text (str "SpeedY: " velY "   speedX: " velX) 400 110)
    ))

(defn drawplayer
  [state]
  (let [x (:x state)
        y (:y state)
        color (:color state)]
    (q/fill color 255 255)
    (q/rect x y (first gamestats/playerSize) (second gamestats/playerSize))
    )
  )

(defn httpupdate
  []

  (clojure.edn/read-string (str (:body (client/get "http://127.0.0.1:8082/showgame"))))
  )

#_(defn readsocket
  [socket]
  (let [reader (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        rawlist (.readLine reader)
        statelist (clojure.edn/read-string (str rawlist))]
    (conj statelist {:server socket})
    ))

(defn drawball
  [state]
  ;Draw baaaaaaalll
  (let [x (:x state)
        y (:y state)
        color (:color state)
        velX (Math/sqrt (Math/pow (:velX state) 2))
        velY (:velY state)]

    (dorun  (cond
              (> 15 velX) (q/fill  0 255 255)
              (and (<= 15 velX) (> 10 velX)) (q/fill 0 125 255)
              (and (<= 10 velX) (> 4 velX)) (q/fill 0 0 255)
              :else (q/fill 0 0 0))
            (q/rect x y (first gamestats/ballsize) (second gamestats/ballsize))
            )))

(defn draw
  [statelist]
  ;map through the list of states, and draw them. and returns the updated list. DOrun is there to make the lazy map do stuff
  (q/clear)
  (dorun (map (fn [state] (let [role (:role state)]
                            (cond (= "ball" role) (do (drawball state) (writeballspeed state))
                                  (= "player" role) (drawplayer state)
                                  (= "enemy" role) (drawplayer state)
                                  (= "score" role) (writescore state)))
                ) statelist))
  )
(defn gameupdate
  [_]
  #_(let [socket (first (filter (fn [state] (= "server" (:role state))) statelist))
        statelist (readsocket socket)]
    statelist
  )
  (httpupdate)

  )

(defn gamesetup
  []
  (do (q/frame-rate 120)
      (q/color-mode :hsb))

  gamestats/mock-state
  )


(defn client_game_start
  [_]
  #_(swap! moo (fn [_] statelist))
  (println @moo)
  (q/defsketch game
               :title "PingPong"
               :size [800 600]
               :setup gamesetup
               :draw draw
               :update gameupdate
               :features [:keep-on-top]
               :middleware [m/fun-mode])


  )