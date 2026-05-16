package utils

import (
	"reflect"

	"hei-gin/core/constants"
)

// StripSystemFields returns a new map with base system fields (and extra fields) removed.
// Mirrors fastapi's strip_system_fields(data, extra_fields=None).
func StripSystemFields(data map[string]interface{}, extraFields ...map[string]bool) map[string]interface{} {
	result := make(map[string]interface{}, len(data))
	exclude := make(map[string]bool)
	for k := range constants.BaseSystemFields {
		exclude[k] = true
	}
	for _, extra := range extraFields {
		for k := range extra {
			exclude[k] = true
		}
	}
	for k, v := range data {
		if !exclude[k] {
			result[k] = v
		}
	}
	return result
}

// ApplyUpdate copies non-zero fields from src to dst (both must be ptr to struct).
func ApplyUpdate(dst, src interface{}, extraProtected ...map[string]bool) {
	dstVal := reflect.ValueOf(dst).Elem()
	srcVal := reflect.ValueOf(src).Elem()

	protected := make(map[string]bool)
	for _, extra := range extraProtected {
		for k := range extra {
			protected[k] = true
		}
	}

	for i := range dstVal.NumField() {
		dstField := dstVal.Field(i)
		srcField := srcVal.Field(i)

		if !srcField.IsZero() {
			if protected[dstVal.Type().Field(i).Name] {
				continue
			}
			if dstField.CanSet() {
				dstField.Set(srcField)
			}
		}
	}
}
