; Copyright (C) 2013  Jussi Nieminen

; This program is free software; you can redistribute it and/or modify
; it under the terms of the GNU General Public License as published by
; the Free Software Foundation; either version 2 of the License, or
; (at your option) any later version.

; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
; GNU General Public License for more details.

; You should have received a copy of the GNU General Public License along
; with this program; if not, write to the Free Software Foundation, Inc.,
; 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.


(ns com.space-ventures.character
  (:require [clojure.algo.generic.math-functions :as math])
  (:import [com.badlogic.gdx Gdx Input$Keys]
           [com.badlogic.gdx.graphics Texture]
           [com.badlogic.gdx.graphics.g2d SpriteBatch]
           [com.badlogic.gdx.math Rectangle Vector3]))

(def CHANGE-WALK-MODE-DIST 1)
(def PI 3.14159)

(defn create [x y width height texture-base scale]
  (hash-map :texture (hash-map :still (Texture. (.internal (Gdx/files) (str texture-base "_still.png")))
                               :walk1 (Texture. (.internal (Gdx/files) (str texture-base "_walk1.png")))
                               :walk2 (Texture. (.internal (Gdx/files) (str texture-base "_walk2.png"))))
            :area (Rectangle. (- x (/ width 2)) (- y (/ height 2)) width height)
            :walk-state 1
            :walk-state-dist 0
            :x x
            :y y
            :width width
            :height height
            :movement (hash-map :target-x -1 :target-y -1 :direction 0 :speed 0)
            :scale scale))

(defn- r2d [rad]
  (* rad (/ 180 PI)))

(defn- d2r [deg]
  (* deg (/ PI 180)))


(defn- get-movement-from-touch [character touch-pos x y]
  (assoc
    character :movement
    (assoc
      (character :movement)
      :target-x (. touch-pos x)
      :target-y (. touch-pos y)
      :direction
      (cond
        ; here we need to first take care of special cases (movement
        ; along x or y axis) before trying to calculate the atan
        (= (. touch-pos x) x)
        (if (> (. touch-pos y) y)
          0 180)
        (= (. touch-pos y) y)
        (if (> (. touch-pos x) x)
          270 90)
            
        ; no special case, calculate the direction with atan
        :else
        (let [deg (r2d (math/atan
                         (/ (- (. touch-pos y) y)
                            (- (. touch-pos x) x))))]
          (cond
            ; then one last thing, we need the check the sector
            ; we are dealing with... atan is annoying...
            (and (< (. touch-pos x) x) (> (. touch-pos y) y))
            (- 90 (- deg))
            (and (< (. touch-pos x) x) (< (. touch-pos y) y))
            (+ 90 deg)
            (and (> (. touch-pos x) x) (< (. touch-pos y) y))
            (- 270 (- deg))
            (and (> (. touch-pos x) x) (> (. touch-pos y) y))
            (+ 270 deg))))
      :speed 7)))

(defn- target-matches-location? [tx ty x y]
  (and (= (int tx) (int x)) (= (int ty) (int y))))





(defn- get-movement [character camera]
  (let [touch-pos (Vector3.)
          x (character :x)
          y (character :y)]
    (if (.isTouched Gdx/input)
      (do
        ; convert the touch coordinates (from input) into the
        ; coordinate system of the camera
        (.set touch-pos (.getX Gdx/input) (.getY Gdx/input) 0)
        (.unproject camera touch-pos)
        (get-movement-from-touch character touch-pos x y))
    
      ; else (no touch):
      (let
        [dir
         (cond
           (.isKeyPressed Gdx/input Input$Keys/UP) 0
           (.isKeyPressed Gdx/input Input$Keys/RIGHT) 270
           (.isKeyPressed Gdx/input Input$Keys/DOWN) 180
           (.isKeyPressed Gdx/input Input$Keys/LEFT) 90
           :else -1)
         movement (character :movement)
         target-x (movement :target-x)
         target-y (movement :target-y)
         speed (movement :speed)]
      
        (if (>= dir 0)
          (assoc
            character :movement
            (assoc
              movement
              :direction dir
              :speed 7
              :target-x -1
              :target-y -1))
          ; no keys pressed, no mouse input, check if we need to clear speed
          (if (and (>= speed 0)
                   (or (< target-x 0)
                       (target-matches-location? target-x target-y x y)))
            (assoc
              character :movement
              (assoc movement
                     :speed 0
                     :target-x -1
                     :target-y -1))
            ; else (no need to clear speed or anything) return old character
            character))))))


; walk-state means which texture (1 or 2) to use.
; walk-state-dist keeps track of how far the character has walked to and when
; a limit is exceeded, the other texture will be used.
(defn move [character-arg delta-time obstacles camera]
  (let [character (get-movement character-arg camera)
        movement (character :movement)
        target-x (movement :target-x)
        target-y (movement :target-y)
        direction (movement :direction)
        speed (movement :speed)
        distance (* speed delta-time)
        dx (* (math/sin (d2r direction)) (- 0 distance))
        dy (* (math/cos (d2r direction)) distance)
        newx (+ (character :x) dx)
        newy (+ (character :y) dy)
        new-walk-dist (if (>= speed 0)
                        (+ distance (character :walk-state-dist))
                        0)
        new-walk-state (if (> new-walk-dist CHANGE-WALK-MODE-DIST)
                         (+ (mod (character :walk-state) 2) 1)
                         (character :walk-state))
        area (character :area)
        scale (character :scale)]
    
    ; update the character area to test for collisions
    (set! (. area x) (- newx (/ (character :width) 2)))
    (set! (. area y) (- newy (/ (character :height) 2)))
    (if (some (fn [obstacle] (.overlaps obstacle area)) obstacles)
      ; there is an obstacle that overlaps with the new area. Don't move!
      (assoc character :movement
             (assoc movement
                    :speed 0
                    :direction direction
                    :target-x -1
                    :target-y -1)
             :walk-state-dist 0)
      (assoc character :movement
             (assoc movement :speed speed :direction direction)
             :x newx :y newy :walk-state new-walk-state
             :walk-state-dist (mod new-walk-dist CHANGE-WALK-MODE-DIST)))))


(defn render [character ^SpriteBatch batch]
  ; move the character's position based on speed and directions
  (let [movement (character :movement)
        speed (movement :speed)
        texture (if (= speed 0)
                  ((character :texture) :still)
                  (if (= (character :walk-state) 1)
                    ((character :texture) :walk1)
                    ((character :texture) :walk2)))
        texture-width (.getWidth texture)
        texture-height (.getHeight texture)
        x-middle (character :x)
        y-middle (character :y)
        x (- x-middle (/ texture-width 2))
        y (- y-middle (/ texture-height 2))
        direction (movement :direction)
        scale (character :scale)]
    ; arguments for the monster draw:
    ; x-pos, y-pos, origin-x, origin-y, width, height, scale-x, scale-y,
    ; rotation, texture-region-start-x, texture-region-start-y,
    ; region-width, region-height, flip-x, flip-y.....
    (.draw batch texture x y (/ texture-width 2) (/ texture-height 2)
      texture-width texture-height scale scale direction 0 0
      texture-width texture-height false false)))


(defn dispose [character]
  (doseq [texture (vals (character :texture))]
    (.dispose texture)))

