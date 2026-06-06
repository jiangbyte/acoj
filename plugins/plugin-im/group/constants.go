package group

// Group types
const (
	GroupTypeMixed       = "mixed"        // B端群主, 可含B/C成员
	GroupTypeConsumerOnly = "consumer_only" // C端群主, 仅C端成员
)

// Member roles
const (
	RoleOwner  = "owner"
	RoleAdmin  = "admin"
	RoleMember = "member"
)

// Member statuses
const (
	MemberActive = "active"
	MemberLeft   = "left"
	MemberKicked = "kicked"
)

// Group statuses
const (
	GroupNormal    = "normal"
	GroupDissolved = "dissolved"
)

// User type constants
const (
	UserTypeBusiness = "BUSINESS"
	UserTypeConsumer = "CONSUMER"
)
