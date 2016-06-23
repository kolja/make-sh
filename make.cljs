#!/usr/bin/env planck
(ns make.core
  (:require [planck.shell :refer [sh]]
            [planck.core :refer [*command-line-args*]]))

(def env {:image "image"
          :container "container"
          :port 8080
          :release-image "release-image"})

(defn dbuild []
  (sh "docker" "build" "-t" (:image env) "."))

(defn drm []
  (sh "docker" "rm" "-fv" (:container env) "||" "true"))

(defn drun []
  (sh "docker" "run" "-dt" "--name" (:container env)
      "-p" (str (-> env :port) ":8080")
      (-> env :image)))

(defn dtag [args]
  (sh "docker" "tag" (:image env) (:release-image env) (first args)))

(defn dpush [args]
  (sh "docker" "push" (:release-image env) (first args)))

(defn deploy [args]
  (sh "senza" "create" (str (:container env) ".yaml") (first args) (second args)))

(defn dshell []
  (sh "docker" "exec" "-it" (:container env) "bash"))

(defn dlogs []
  (sh "docker" "logs" "-f" (:container env)))

(defn generate-deploy-key []
  (sh "ssh-keygen" "-t" "rsa" "-b" "4096" "-C" "deploy-key" "-f" "./deploy-key.id_rsa" "-N"))

(defn not-found []
  "When you enter a subcommand that is not in the shortcuts, this will be printed:"
  {:out (str "No such subcommand: \"" (first *command-line-args*) "\"\n")})

; Shortcuts
(def shortcuts {:b [dbuild]
                :r [drm drun]
                :l [dlogs]
                :s [dshell]
                :drm [drm]
                :dp [dtag dpush]
                :br [dbuild drun]
                :brl [dbuild drun dlogs]
                :brs [dbuild drun dshell]
                :rl [drun dlogs]})

(defn process [f args]
  (f args))

(defn p [output]
   (doseq [o output] (print (:out o))))

(defn execute [cmd-line-args shortcuts]
  (let [cmds ((keyword (first cmd-line-args)) shortcuts [not-found])
        args (rest cmd-line-args)]
    (p (map process cmds (repeat args)))))

(execute *command-line-args* shortcuts)

