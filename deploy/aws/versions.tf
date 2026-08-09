terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.80"
    }
  }

  # State must not live on a laptop. Uncomment and supply the bucket/table with
  # `tofu init -backend-config=backend.hcl` once they exist:
  #
  #   backend "s3" {
  #     bucket         = "<state-bucket>"
  #     key            = "nightflow/eks/terraform.tfstate"
  #     region         = "<region>"
  #     dynamodb_table = "<lock-table>"
  #     encrypt        = true
  #   }
}
