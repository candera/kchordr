(ns kchordr.pinvoke
  "Generate functions that will let you call into native code via Pinvoke.

  Examples:

  (def beep (pinvoke \"user32.dll\"
                     \"MessageBeep\"
                     Boolean
                     [UInt32]
                     CallingConvention/Winapi))

  (beep (uint 0x00000040))

  (def message-box (pinvoke \"user32.dll\"
                             \"MessageBox\"
                             Int32
                             [IntPtr String String UInt32]
                             CallingConvention/Winapi))

  (message-box nil \"foo\" \"bar\" (uint 6))"
  (:import (System.Reflection AssemblyName CallingConventions TypeAttributes
                              MethodAttributes FieldInfo )
           (System.Reflection.Emit AssemblyBuilderAccess CustomAttributeBuilder)
           (System.Runtime.InteropServices CallingConvention CharSet DllImportAttribute)))

(def dia-field-defaults
  {"ExactSpelling" true
   "PreserveSig" true
   "SetLastError" true
   "CallingConvention" CallingConvention/StdCall
   "CharSet" CharSet/Auto
   "BestFitMapping" true
   "ThrowOnUnmappableChar" true})

(defn fieldify
  "Given a type and map of field names to values, return a map of
  FieldInfos for that type to those same values."
  [typ m]
  (into {} (map (fn [[k v]] [(.GetField typ k) v]) m)))

(defn dia-fields
  "Return a map of FieldInfos to the values they should have for a
  DllImportAttribute instance."
  [entry-point calling-convention]
  (fieldify DllImportAttribute (assoc dia-field-defaults
                                 "EntryPoint" entry-point
                                 "CallingConvention" calling-convention)))

(defn pinvoke
  "Return a function that will call the function `entry-point` in the dll
  `dll`. `return-type` must be a Type, `arg-types` must be a vector of
  Type objects, and calling-convention should be of type
  System.Runtime.InteropServices.CallingConvention."
  [dll entry-point return-type arg-types calling-convention]
  (let [assembly-name (str (gensym "assembly"))
        assembly-builder (.DefineDynamicAssembly AppDomain/CurrentDomain
                                                 (AssemblyName. assembly-name)
                                                 AssemblyBuilderAccess/Run)
        module-builder (.DefineDynamicModule assembly-builder "interop")
        type-builder (.DefineType module-builder
                                  "InteropModule"
                                  (enum-or TypeAttributes/Public
                                           TypeAttributes/UnicodeClass))
        ;; We can't use DefinePInvokeMethod because it doesn't let you
        ;; do enough of DllImportAttribute, like SetLastError.
        method-builder (.DefineMethod type-builder
                                      entry-point
                                      (enum-or MethodAttributes/Public
                                               MethodAttributes/HideBySig
                                               MethodAttributes/Static)
                                      return-type
                                      (into-array Type arg-types))

        ctor (.GetConstructor DllImportAttribute (into-array Type [String]))
        dia-fields (dia-fields entry-point calling-convention)
        dia (CustomAttributeBuilder. ctor
                                     (into-array Object [dll])
                                     (into-array FieldInfo (keys dia-fields))
                                     (into-array Object (vals dia-fields)))
        _ (.SetCustomAttribute method-builder dia)
        typ ^Type (.CreateType type-builder)
        method (.GetMethod typ entry-point)]
    (fn [& args]
      (.Invoke method nil (into-array Object args)))))

(defn get-last-error
  "A thin wrapper around the Win32 GetLastError API."
  []
  (System.Runtime.InteropServices.Marshal/GetLastWin32Error))
