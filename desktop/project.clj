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


(defproject space-ventures "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/algo.generic "0.1.1"]
                 [com.badlogicgames.gdx/gdx "0.9.9-SNAPSHOT"]
                 [com.badlogicgames.gdx/gdx-backend-lwjgl "0.9.9-SNAPSHOT"]
                 [com.badlogicgames.gdx/gdx-platform "0.9.9-SNAPSHOT"
                  :classifier "natives-desktop"]]
  :repositories [["sonatype"
                  "https://oss.sonatype.org/content/repositories/snapshots/"]
                 ["mini2Dx-thirdparty"
                  "http://mini2dx.org/nexus/content/repositories/thirdparty"]
                 ["mini2Dx"
                  "http://mini2dx.org/nexus/content/repositories/releases"]]
  
  :source-paths ["src/clojure" "src-common/clojure"]
  :java-source-paths ["src/java" "src-common/java"]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  :aot [com.space-ventures.desktop-launcher]
  :main com.space-ventures.desktop-launcher)
