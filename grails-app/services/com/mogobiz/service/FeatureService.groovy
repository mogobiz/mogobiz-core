package com.mogobiz.service

import com.mogobiz.store.domain.Category
import com.mogobiz.store.domain.Feature
import com.mogobiz.store.domain.FeatureValue
import com.mogobiz.store.domain.Product

class FeatureService {
	static transactional = true

	List<Feature> getCategoryFeatures(long categoryId, boolean includeParents) {
		List<Feature> features = Feature.withCriteria {
			category { eq ('id', categoryId) }
			isNull("product")
			order('position', "asc")
		}
		if (includeParents) {
			Set<Long> cats = getCategories(categoryId)
			List<Feature>  parentFeatures = getFeatures(cats)
			features.addAll(0, parentFeatures)
		}
		return features
	}

	private Set<Long> getCategories(Long categoryId) {
		Set<Long> cats = []
		Category category = Category.get(categoryId)
		while (category.parent != null) {
			category = category.parent
			cats << category.id
		}
		return cats
	}

	private List<Feature> getFeatures(Set<Long> cats) {
		if (cats.size() > 0) {
			List<Feature>  features = Feature.withCriteria {
				category {
					inList ('id', cats.toList())
				}
				isNull("product")
				order('position', "asc")
			}
			return features
		}
		else {
			return []
		}
	}

	List<Feature> getProductFeatures(long productId, boolean includeParents) {
		List<Feature> features = Feature.withCriteria {
			product { eq ('id', productId) }
			order('position', "asc")
		}
		Product product = Product.get(productId)
		if (includeParents) {
			Set<Long> cats = [product.category.id]
			cats.addAll(getCategories(product.category.id))
			List<Feature>  parentFeatures = getFeatures(cats)
			features.addAll(0, parentFeatures)
		}
		List<FeatureValue> values = FeatureValue.findAllByProduct(product)
		values.each {FeatureValue value ->
			Feature feature = features.find {f -> f.id == value.feature.id}
			if (feature != null) {
				feature.value = feature.value + "||||" + value.value
				feature.discard()
			}
		}
		return features
	}

	boolean existFeatureInCategory(String featureName, long featureId, long categoryId, boolean includeParents) {
		List<Feature> features = Feature.withCriteria {
			eq('name', featureName)
			if (featureId) {
				ne("id", featureId)
			}
			category { eq('id', categoryId) }
		}
		if (!features.isEmpty())
			return true
		if (includeParents) {
			Set<Long> cats = getCategories(categoryId)
			if (cats.size() > 0) {
				features = Feature.withCriteria {
					eq('name', featureName)
					if (featureId) {
						ne("id", featureId)
					}
					category {
						inList ('id', cats.toList())
					}
				}
			}
			return !features.isEmpty()
		}
		return false
	}

	boolean existFeatureInProduct(String featureName, Long featureId,long productId, boolean includeParents) {
		List<Feature> features = Feature.withCriteria {
			eq('name', featureName)
			if (featureId) {
				ne("id", featureId)
			}
			product { eq('id', productId) }
		}
		if (!features.isEmpty())
			return true
		if (includeParents) {
			Product product = Product.get(productId)
			Set<Long> cats = []
			cats.addAll(getCategories(product.category.id))
			if (cats.size() > 0) {
				features = Feature.withCriteria {
					eq('name', featureName)
					if (featureId) {
						ne("id", featureId)
					}
					category {
						inList ('id', cats.toList())
					}
				}
			}
			return !features.isEmpty()
		}
		return false
	}
}

