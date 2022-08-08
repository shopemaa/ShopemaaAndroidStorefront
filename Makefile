.PHONY: pullSchema

pullSchema:
	./gradlew :app:downloadApolloSchema --endpoint='https://api.shopemaa.com/query' --schema=app/src/main/graphql/schema.graphqls
