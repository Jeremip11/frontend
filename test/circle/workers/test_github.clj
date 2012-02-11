(ns circle.workers.test-github
  (:use circle.backend.build.test-utils)
  (:require [circle.model.build :as build])
  (:use [circle.util.mongo])
  (:require [somnium.congomongo :as mongo])
  (:use midje.sweet)
  (:use circle.workers.github))

(test-ns-setup)

(fact "start-build-from-hook works with dummy project"
  (let [json circle-dummy-project-json-str
        build (start-build-from-hook nil nil nil json)]
    (-> @build :vcs_url) => truthy
    (-> build (build/successful?)) => true
    (-> @build :build_num) => integer?))

(fact "builds started from the hook have a start time"
  ;; this test added because of production failure, 2012/02/10
  (let [json circle-dummy-project-json-str
        build (start-build-from-hook nil nil nil json)
        build-row (mongo/fetch-one :builds :where {:_id (-> @build :_id)})]
    (-> @build :start_time) => truthy
    build-row => truthy
    (-> build-row :start_time) => truthy))

(fact "authorization-url works"
  (-> "http://localhost:3000/hooks/repos" authorization-url) =>
  "https://github.com/login/oauth/authorize?client_id=586bf699b48f69a09d8c&scope=repo&redirect_uri=http%3A%2F%2Flocalhost%3A3000%2Fhooks%2Frepos"
  (provided (circle.env/env) => :test))