;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; A scratch file for playing around
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(+ 2 3)

Attribute

(import '[System.Reflection MethodAttributes])

MethodAttributes/PinvokeImpl

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Objective: call MessageBeep via Pinvoke

;; User32.dll
;; 
;; BOOL WINAPI MessageBeep(
;;  __in  UINT uType
;; );

(in-ns 'kchordr.pinvoke)

(def beep (pinvoke "user32.dll"
                   "MessageBeep"
                   Boolean
                   [UInt32]
                   CallingConvention/Winapi))

(beep (uint 0x00000040))


(def message-box (pinvoke "user32.dll"
                          "MessageBoxW"
                          Int32
                          [IntPtr String String UInt32]
                          CallingConvention/Winapi))

(message-box nil "foo" "bar" (uint 6))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Objective: pass a structure to an APIcall GetCursorPos.
;; Demonstrates marshalling structures and ref arguments.

;; BOOL WINAPI GetCursorPos(
;;   __out  LPPOINT lpPoint
;; );
;;
;; typedef struct tagPOINT {
;;   LONG x;
;;   LONG y;
;; } POINT, *PPOINT;

(import 'System.Reflection.FieldAttributes)

(defn interop-type [spec]
  (let [assembly-name (str (gensym "assembly"))
        assembly-builder (.DefineDynamicAssembly AppDomain/CurrentDomain
                                                 (AssemblyName. assembly-name)
                                                 AssemblyBuilderAccess/Run)
        module-builder (.DefineDynamicModule assembly-builder "interop")
        type-builder (.DefineType module-builder
                                  "InteropModule"
                                  (enum-or TypeAttributes/Public
                                           TypeAttributes/UnicodeClass))]
    (doseq [[name type] (partition 2 spec)]
      (.DefineField type-builder
                    name
                    type
                    FieldAttributes/Public))
    (.CreateType type-builder)))

;; (def tbld  (interop-type ["x" Int32 "y" Int32]))
;; (.DefineField tbld "x" Int32 (into-array FieldAttributes [FieldAttributes/Public]))

(def point-type (interop-type ["x" Int32 "y" Int32]))

(def point (Activator/CreateInstance point-type))

(.x point)

(set! (. point x) (int 3))              ; Doesn't work - type mismatch

(.x point)
(.x (Activator/CreateInstance point-type))

(s)



(System.Reflection.Assembly/LoadWithPartialName "System.Drawing")
(import 'System.Drawing.Point)

(Point. 12 15)

(. (Point. 12 14) X)

(def get-cursor-pos (pinvoke "user32.dll" "GetCursorPos"
                             Boolean
                             []))

