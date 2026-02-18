FROM liquibase/liquibase:5.0.1

COPY src/main/resources/db/changelog /liquibase/changelog

USER root
RUN curl -L -o /liquibase/lib/postgresql.jar \
    https://jdbc.postgresql.org/download/postgresql-42.7.1.jar
USER liquibase

ENTRYPOINT ["liquibase"]
CMD ["--changelog-file=changelog/changelog-master.yaml", "update"]