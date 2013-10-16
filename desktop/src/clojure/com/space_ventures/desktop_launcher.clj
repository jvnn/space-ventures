(ns com.space-ventures.desktop-launcher
  (:require [com.space-ventures.core])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication])
  (:gen-class))

(defn -main
  []
  (LwjglApplication. (com.space-ventures.core.Game.) "space-ventures" 640 640 false))
