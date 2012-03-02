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






