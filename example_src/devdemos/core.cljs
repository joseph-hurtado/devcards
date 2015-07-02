(ns ^:figwheel-always devdemos.core
    (:require
     [om.core :as om :include-macros true]
     [om.dom :as dom :include-macros true]
     [reagent.core :as reagent]
     [clojure.string :as string]
     [sablono.core :as sab :include-macros true]
     [devdemos.defcard-api]
     [devdemos.two-zero]
     [devdemos.testing]
     [devdemos.errors]
     [devdemos.extentions]
     [cljs.test :as t :include-macros true :refer-macros [testing is]])
    (:require-macros
     ;; Notice that I am not including the 'devcards.core namespace
     ;; but only the macros. This helps ensure that devcards will only
     ;; be created when the :devcards is set to true in the build config.
     [devcards.core :as dc :refer [defcard defcard-doc noframe-doc deftest dom-node]]))

(enable-console-print!)

(devcards.core/start-devcard-ui!)

(defcard-doc
  "# Devcards: the hard sell
    
   Devcards is intended to make ClojureScript development a pure joy.
 
   Devcards are intended to facilitate **interactive live
   development**. Devcards can be used in conjuntion with figwheel but
   will also work with any form of live file reloading.
 
   Devcards revolves around a multi-purpose macro called `defcard`.
   You can think of `defcard` a powerful form of pprint that helps you display
   code examples as an organized set of cards. 

   The Devcards you create are intended to have no impact on the size
   of your production code. You can use devcards just as you would use
   exectuable comments. You can also keep them seperate like a test
   suite.

   Devcards configuration couldn't be simpler. Just add `:figwheel
   {:devcards true}` to your build config.

   Let's look at an advanced Devcard:"

  (dc/mkdn-pprint-code
   '(defcard bmi-calculator
      "*Code taken from the Reagent readme.*"
      (fn [_ data-atom] (bmi-component data-atom))
      {:height 180 :weight 80}
      {:inspect-data true :history true}))

  "And you can see this devcard rendered below:")

;; code from the reagent page adapted to plain reagent
(defn calc-bmi [bmi-data]
  (let [{:keys [height weight bmi] :as data} bmi-data
        h (/ height 100)]
    (if (nil? bmi)
      (assoc data :bmi (/ weight (* h h)))
      (assoc data :weight (* bmi h h)))))

(defn slider [bmi-data param value min max]
  (sab/html
   [:input {:type "range" :value value :min min :max max
            :style {:width "100%"}
            :on-change (fn [e]
                         (swap! bmi-data assoc param (.-target.value e))
                         (when (not= param :bmi)
                           (swap! bmi-data assoc :bmi nil)))}]))

(defn bmi-component [bmi-data]
  (let [{:keys [weight height bmi]} (calc-bmi @bmi-data)
        [color diagnose] (cond
                          (< bmi 18.5) ["orange" "underweight"]
                          (< bmi 25) ["inherit" "normal"]
                          (< bmi 30) ["orange" "overweight"]
                          :else ["red" "obese"])]
    (sab/html
     [:div 
      [:h3 "BMI calculator"]
      [:div
       "Height: " (int height) "cm"
       (slider bmi-data :height height 100 220)]
      [:div
       "Weight: " (int weight) "kg"
       (slider bmi-data :weight weight 30 150)]
      [:div
       "BMI: " (int bmi) " "
       [:span {:style {:color color}} diagnose]
       (slider bmi-data :bmi bmi 10 50)]])))

(defcard bmi-calculator
  "*Code taken from the Reagent readme.*"
  (fn [_ data-atom] (bmi-component data-atom))
  {:height 180 :weight 80}
  {:inspect-data true :history true})

(defcard-doc
  "## Time travel

   Please interact with **the BMI calculator above**. As you change
   the sliders you will notice that a "
   (str "<span class='com-rigsomelight-devcards-history-control-left'></span>")
   "shows up in the upper right hand corner.

   That is a history control widget. This is part of the devcard and
   can be enabled by adding `{:history true}` to the devcard options.
   
   Go ahead and move the sliders and play with the history control
   widget. *add instructions for other controls here*

   ## Data display

   You will also notice that the data from the main data store is
   being displayed. This is enabled by adding `{:inspect-data true}`
   to the devcard options.
 
   There if you interact with the calculator above you will see that
   the integers are being stored as strings in the data atom. This is
   a smell that you will see immediately when the data is displayed front
   and center like this.

   ## Markdown docs
   
   The phrase \"*Code taken from the Reagent readme.*\" is optional in
   the example above. But allows for the easy display of contextual
   documentation.

   ## Auto-detection
   
   The `defcard` macro does its best to display the data given to it.
   You can pass `defcard` a **string**, a **ReactElement**, a **Map**, a **Vector**, a
   **List**, an **Atom**, an **RAtom**, an **IDeref** and expect
   various cursor implementations to work soon as well.
   " )

(deftest cljs-test-integration
  "# clsj.test integration

   Devcards provides a `deftest` macro that behaves very similarly to
   the `cljs.test/deftest` macro. This makes it easy to define tests
   that both show up in the Devcards display and can be run
   using `(run-tests)` as well.
  
   The test card has controls in the upper right hand corner that not
   only summerize testing status but also allow you to focus on passing or
   failing tests.

   Go ahead and click on the numbers in the header of this card.
  
   The test card will display the testing context as well as the
   messages for the various tests that run."
  (testing "testing context 1"
    (is (= (+ 3 4 55555) 4) "This is the message arg to an 'is' test")
    (is (= (+ 1 0 0 0) 1) "This should work")
    (is (= 1 3))              
    (is false)
    (is (throw "errors get an extra red line on the side")))
  "Top level strings are interpreted as markdown for inline documentation."
  (t/testing "testing context 2"
    (is (= (+ 1 0 0 0) 1))        
    (t/is (= (+ 3 4 55555) 4))
    (t/is false)
    (t/testing "nested context"
      (is (= (+ 1 0 0 0) 1))        
      (t/is (= (+ 3 4 55555) 4))
      (t/is false))))


(defn om-slider [bmi-data param value min max]
  (sab/html
   [:input {:type "range" :value value :min min :max max
            :style {:width "100%"}
            :on-change (fn [e]
                         (om/update! bmi-data param (.-target.value e))
                         (when (not= param :bmi)
                           (om/update! bmi-data :bmi nil)))}]))

(defn om-bmi-component [bmi-data owner]
  (let [{:keys [weight height bmi]} (calc-bmi bmi-data)
        [color diagnose] (cond
                          (< bmi 18.5) ["orange" "underweight"]
                          (< bmi 25) ["inherit" "normal"]
                          (< bmi 30) ["orange" "overweight"]
                          :else ["red" "obese"])]
    (om/component
     (sab/html
      [:div 
       [:h3 "BMI calculator"]
       [:div
        "Height: " (int height) "cm"
        (om-slider bmi-data :height height 100 220)]
       [:div
        "Weight: " (int weight) "kg"
        (om-slider bmi-data :weight weight 30 150)]
       [:div
        "BMI: " (int bmi) " "
        [:span {:style {:color color}} diagnose]
        (om-slider bmi-data :bmi bmi 10 50)]]))))

(defcard om-support
  "# Om support

   Here is the same calculator being rendered as an Om application.

   ```
   (defcard om-support
     (dc/om-root om-bmi-component)
     {:height 180 :weight 80} ;; initial data
     {:inspect-data true :history true })
   ``` 
   "
  (dc/om-root om-bmi-component)
  {:height 180 :weight 80} ;; initial data
  {:inspect-data true
   :history true })


(defonce re-bmi-data (reagent/atom {:height 180 :weight 80}))

(defn re-slider [param value min max]
  [:input {:type "range" :value value :min min :max max
           :style {:width "100%"}
           :on-change (fn [e]
                        (swap! re-bmi-data assoc param (.-target.value e))
                        (when (not= param :bmi)
                          (swap! re-bmi-data assoc :bmi nil)))}])

(defn re-bmi-component []
  (let [{:keys [weight height bmi]} (calc-bmi @re-bmi-data)
        [color diagnose] (cond
                          (< bmi 18.5) ["orange" "underweight"]
                          (< bmi 25) ["inherit" "normal"]
                          (< bmi 30) ["orange" "overweight"]
                          :else ["red" "obese"])]
    [:div
     [:h3 "BMI calculator"]
     [:div
      "Height: " (int height) "cm"
      [re-slider :height height 100 220]]
     [:div
      "Weight: " (int weight) "kg"
      [re-slider :weight weight 30 150]]
     [:div
      "BMI: " (int bmi) " "
      [:span {:style {:color color}} diagnose]
      [re-slider :bmi bmi 10 50]]]))

(defcard reagent-support
  "# There is also built-in support for Reagent 

  Below is the same BMI calculator in Reagent:
  ```
  (defcard reagent-support
    (dc/reagent re-bmi-component)
    re-bmi-data ;; reagent atom
    {:inspect-data true :history true })
  ```"
  (dc/reagent re-bmi-component)
  re-bmi-data
  {:inspect-data true :history true })

(defcard
  "# Not cool enough?
   
   Well there is a bunch more, but that's what the docs are for.

   For quick documentation please see the source for these devcards here.

   ## Quick Start

   These are brief instructions for the curious these will not be
   helpful if you do not have a lot of experience with ClojureScript.
   
   You can generate a new devcards project with:
   ```
   lein new devcards hello-world
   ```

   ## Existing project



   Integrating devcards into an existing is fairly straightforward and
   requires a seperate build, similar to how you would create a test
   build.

   Require the devcards macros: 
   ```
   (ns example.core
    (:require-macros
     ;; Notice that I am not including the 'devcards.core namespace
     ;; but only the macros. This helps ensure that devcards will only
     ;; be created when the :devcards is set to true in the build config.
     [devcards.core :as dc :refer [defcard deftest]]))
   ```

   If you are using figwheel you can just copy your figwheel dev build
   and add `:devcards true` (figwheel >= 0.3.5) to your `:figwheel`
   build config like so:

   ```
   :cljsbuild {
     :builds [{:id :devcards
               :source-paths [\"src\"]
               :figwheel { :devcards true }
               :compiler {
                 :main \"example.core\"
                 :asset-path \"js/out\"
                 :output-to \"resources/public/js/example.js\"
                 :output-dir \"resources/public/js/out\"
               }}]}
   ```
 
  It's important to make sure that your application isn't launching
  itself on load. Otherwise the devcards application will try to start
  at the same time your applicatoin is starting.
  
  For now refer to the devcards-template for a simple pattern to
  have your application start conditionally.
  
  
  ")
