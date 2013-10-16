(ns com.space-ventures.character
  (:require [clojure.algo.generic.math-functions :as math])
  (:import [com.badlogic.gdx Gdx]
           [com.badlogic.gdx.graphics Texture]
           [com.badlogic.gdx.graphics.g2d SpriteBatch]
           [com.badlogic.gdx.math Rectangle]))

(defn create [x y width height texture-base]
  (hash-map :texture (hash-map :still (Texture. (.internal (Gdx/files) (str texture-base "_still.png")))
                               :walk1 (Texture. (.internal (Gdx/files) (str texture-base "_walk1.png")))
                               :walk2 (Texture. (.internal (Gdx/files) (str texture-base "_walk2.png"))))
            :x x
            :y y
            :speed 0
            :direction 0))

(defn move [character direction speed delta-time]
  (let [newdir (if (>= direction 0)
                 direction
                 (character :direction))
        deg2rad (/ 3.14159 180)
        ; the -/+ and sin/cos below were just decided with trial&error...
        newx (- (character :x) (* (math/sin (* direction deg2rad)) (* speed delta-time)))
        newy (+ (character :y) (* (math/cos (* direction deg2rad)) (* speed delta-time)))]
    
    
    (assoc character :speed speed :direction newdir :x newx :y newy)))

(defn render [character ^SpriteBatch batch]
  ; move the character's position based on speed and directions
  (let [still-texture ((character :texture) :still)
        texture-width (.getWidth still-texture)
        texture-height (.getHeight still-texture)
        x-middle (character :x)
        y-middle (character :y)
        x (- x-middle (/ texture-width 2))
        y (- y-middle (/ texture-height 2))
        direction (character :direction)
        speed (character :speed)]
    ; arguments for the monster draw:
    ; x-pos, y-pos, origin-x, origin-y, width, height, scale-x, scale-y,
    ; rotation, texture-region-start-x, texture-region-start-y,
    ; region-width, region-height, flip-x, flip-y.....
    (.draw batch still-texture x y (/ texture-width 2) (/ texture-height 2)
      texture-width texture-height 1 1 direction 0 0
      texture-width texture-height false false)))




