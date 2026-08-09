data "aws_availability_zones" "available" {
  state = "available"

  filter {
    name   = "opt-in-status"
    values = ["opt-in-not-required"]
  }
}

locals {
  name         = "${var.project}-${var.environment}"
  cluster_name = "${var.project}-${var.environment}"

  azs = slice(data.aws_availability_zones.available.names, 0, var.availability_zone_count)

  tags = merge(
    {
      Project     = var.project
      Environment = var.environment
      ManagedBy   = "opentofu"
      Repository  = "github.com/kaantopcuw/NightFlow"
    },
    var.tags,
  )
}
