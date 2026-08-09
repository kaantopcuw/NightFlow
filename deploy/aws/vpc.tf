# Standard three-tier layout: public subnets for the load balancers, private
# subnets for the nodes. The kubernetes.io/role tags are what lets the AWS load
# balancer controller pick the right subnets automatically.

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.19"

  name = "${local.name}-vpc"
  cidr = var.vpc_cidr

  azs = local.azs

  # /20 per private subnet (4091 usable IPs) - the VPC CNI hands a real VPC
  # address to every pod, so subnets need room.
  private_subnets = [for i in range(var.availability_zone_count) : cidrsubnet(var.vpc_cidr, 4, i)]
  public_subnets  = [for i in range(var.availability_zone_count) : cidrsubnet(var.vpc_cidr, 8, i + 200)]

  enable_nat_gateway   = true
  single_nat_gateway   = var.single_nat_gateway
  enable_dns_hostnames = true
  enable_dns_support   = true

  public_subnet_tags = {
    "kubernetes.io/role/elb" = "1"
  }

  private_subnet_tags = {
    "kubernetes.io/role/internal-elb" = "1"
  }

  tags = local.tags
}
