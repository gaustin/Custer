(ns custer.socket-server)

(defn start-server [port]
  (new java.net.ServerSocket port))

(defn accept-connection [server fun]
   (future (let [socket (.accept server)]
      (fun socket))))

