package utils

import (
	"reflect"

	"hei-gin/core/constants"
)

// StripSystemFields removes base system fields from a map.
func StripSystemFields(data map[string]interface{}) {
	for k := range data {
		if constants.BaseSystemFields[k] {
			delete(data, k)
		}
	}
}

// ApplyUpdate copies non-zero fields from src to dst (both must be ptr to struct).
func ApplyUpdate(dst, src interface{}) {
	dstVal := reflect.ValueOf(dst).Elem()
	srcVal := reflect.ValueOf(src).Elem()

	for i := range dstVal.NumField() {
		dstField := dstVal.Field(i)
		srcField := srcVal.Field(i)

		if !srcField.IsZero() {
			if dstField.CanSet() {
				dstField.Set(srcField)
			}
		}
	}
}
