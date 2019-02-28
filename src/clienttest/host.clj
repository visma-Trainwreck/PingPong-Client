(ns clienttest.host
  (:require [clojure.java.io :as io]
            [clienttest.game :as game]
            [clj-http.client :as client])
  (:import
    (java.io PrintWriter InputStreamReader BufferedReader)
    (java.net Socket)))

(def freenode {:name "irc.freenode.net" :port 6667})
(def user {:name "Nurullah Akkaya" :nick "nakkaya"})

(declare conn-handler)

(defn connect []
  (let [socket (Socket. "127.0.0.1" 9000)
        in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        out (PrintWriter. (.getOutputStream socket))
        conn (ref {:in in :out out :socket socket})]
    (doto (Thread. ^Runnable (conn-handler socket)) (.start))
    conn))
#_(Thread. #(conn-handler conn))
(defn write [conn msg]
  (doto (:out @conn)
    (.println (str msg "\r"))
    (.flush)))

(defn conn-handler [socket]
       (let [reader (BufferedReader. (InputStreamReader. (.getInputStream socket)))
             rawlist (.readLine reader)
             statelist (clojure.edn/read-string (str rawlist))
             updatedlist (conj statelist socket)]
         (game/client_game_start updatedlist)
         ))

(defn login [conn user]
  (write conn (str "NICK " (:nick user)))
  (write conn (str "USER " (:nick user) " 0 * :" (:name user))))




(defn -main
  []
  (game/client_game_start ""))