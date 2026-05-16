package utils

// Node represents a node in a hierarchical tree structure.
type Node struct {
	Name     string
	ParentID string
}

// ResolvePathFromMap traces the parent chain of an entity in a node map
// and returns the name path from root to the entity.
func ResolvePathFromMap(entityID string, nodeMap map[string]Node) []string {
	if entityID == "" {
		return nil
	}

	names := make([]string, 0)
	current := entityID

	for current != "" {
		node, ok := nodeMap[current]
		if !ok {
			break
		}
		names = append(names, node.Name)
		if node.ParentID == "" || node.ParentID == "0" {
			break
		}
		current = node.ParentID
	}

	// Reverse to get root-to-leaf order
	for i, j := 0, len(names)-1; i < j; i, j = i+1, j-1 {
		names[i], names[j] = names[j], names[i]
	}

	return names
}

// ResolveNamePath resolves a hierarchical entity ID to a list of names
// from root to the current entity, using the provided loadFunc to build the node map.
func ResolveNamePath(entityID string, loadFunc func() (map[string]Node, error)) []string {
	if entityID == "" {
		return nil
	}

	nodeMap, err := loadFunc()
	if err != nil {
		return nil
	}

	return ResolvePathFromMap(entityID, nodeMap)
}
