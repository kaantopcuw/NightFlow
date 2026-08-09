# One repository per service, mirroring the image names the GitHub Actions
# workflow produces. Scanning on push is free and catches known CVEs in the base
# image; the lifecycle policy stops old builds from accumulating forever.

module "ecr" {
  source  = "terraform-aws-modules/ecr/aws"
  version = "~> 2.3"

  for_each = toset(var.services)

  repository_name = "${var.project}/${each.value}"

  repository_image_tag_mutability = "MUTABLE"
  repository_image_scan_on_push   = true

  # Dev registries get torn down with the rest of the stack; production ones
  # should not be deletable while they still hold images.
  repository_force_delete = var.environment != "prod"

  create_lifecycle_policy = true
  repository_lifecycle_policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep only the ${var.ecr_image_retention_count} most recent images"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = var.ecr_image_retention_count
        }
        action = {
          type = "expire"
        }
      },
    ]
  })

  tags = local.tags
}
