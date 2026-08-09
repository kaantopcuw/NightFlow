variable "aws_region" {
  description = "Region the cluster and the registries are created in."
  type        = string
  default     = "eu-central-1"
}

variable "project" {
  description = "Name prefix for every resource created by this configuration."
  type        = string
  default     = "nightflow"
}

variable "environment" {
  description = "Environment name; becomes part of the cluster name and of the tags."
  type        = string
  default     = "dev"
}

variable "vpc_cidr" {
  description = "CIDR block of the VPC."
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zone_count" {
  description = "How many availability zones to spread the subnets over."
  type        = number
  default     = 3

  validation {
    condition     = var.availability_zone_count >= 2 && var.availability_zone_count <= 4
    error_message = "EKS needs at least two availability zones; more than four is wasteful here."
  }
}

variable "single_nat_gateway" {
  description = <<-EOT
    One NAT gateway for the whole VPC instead of one per AZ. Cheaper, and a
    single point of failure - acceptable for dev, not for production.
  EOT
  type        = bool
  default     = true
}

variable "kubernetes_version" {
  description = "EKS control plane version."
  type        = string
  default     = "1.31"
}

variable "cluster_endpoint_public_access" {
  description = "Whether the Kubernetes API is reachable from the internet."
  type        = bool
  default     = true
}

variable "cluster_endpoint_public_access_cidrs" {
  description = "CIDRs allowed to reach the public API endpoint. Narrow this down before using it for real."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "node_instance_types" {
  description = "Instance types for the managed node group."
  type        = list(string)
  default     = ["t3.large"]
}

variable "node_group_min_size" {
  description = "Minimum number of worker nodes."
  type        = number
  default     = 2
}

variable "node_group_max_size" {
  description = "Maximum number of worker nodes."
  type        = number
  default     = 5
}

variable "node_group_desired_size" {
  description = "Initial number of worker nodes."
  type        = number
  default     = 3
}

variable "services" {
  description = "Services that get an ECR repository - one per container image."
  type        = list(string)
  default = [
    "config-server",
    "discovery-server",
    "gateway-service",
    "auth-service",
    "venue-service",
    "event-catalog-service",
    "ticket-service",
    "shopping-cart-service",
    "order-service",
    "notification-service",
    "checkin-service",
  ]
}

variable "ecr_image_retention_count" {
  description = "How many images to keep per repository before the lifecycle policy expires the oldest."
  type        = number
  default     = 20
}

variable "tags" {
  description = "Extra tags merged into every resource."
  type        = map(string)
  default     = {}
}
