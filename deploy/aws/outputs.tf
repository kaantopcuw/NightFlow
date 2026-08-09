output "region" {
  description = "Region everything was created in."
  value       = var.aws_region
}

output "cluster_name" {
  description = "EKS cluster name."
  value       = module.eks.cluster_name
}

output "cluster_endpoint" {
  description = "Kubernetes API endpoint."
  value       = module.eks.cluster_endpoint
}

output "cluster_certificate_authority_data" {
  description = "Base64 CA bundle of the cluster."
  value       = module.eks.cluster_certificate_authority_data
  sensitive   = true
}

output "configure_kubectl" {
  description = "Command that writes the cluster into the local kubeconfig."
  value       = "aws eks update-kubeconfig --region ${var.aws_region} --name ${module.eks.cluster_name}"
}

output "ecr_repository_urls" {
  description = "Repository URL per service, keyed by service name."
  value       = { for name, repo in module.ecr : name => repo.repository_url }
}

output "vpc_id" {
  description = "VPC the cluster runs in."
  value       = module.vpc.vpc_id
}

output "private_subnet_ids" {
  description = "Private subnets the nodes are placed in."
  value       = module.vpc.private_subnets
}
