## 개요

이 저장소는 Elasticsearch와 Apache Airflow와 같은 반복적인 오픈 소스 환경을 Jenkins 파이프라인을 사용하여 자동으로 설정하기 위해 개발되었습니다.

## Elasticsearch 환경 자동화

### 배포 옵션

Elasticsearch는 [참조 저장소](https://github.com/cucuridas/elasticsearch_deploy)에서 정의된대로 세 가지 다른 구성으로 배포할 수 있습니다:

1. Single
2. Master-slave
3. Raft-cluster

### 배포 과정

Elasticsearch의 배포 과정은 다음과 같습니다:

![Elasticsearch Jenkins Pipeline](doc_img/jenkins_pipeline_elasticsearch.png)

1. 배포가 의도된 대상 서버에 연결합니다.
2. Docker 컨테이너를 사용하여 Elasticsearch가 이미 배포되었는지 확인합니다.
    - 실행 중인 컨테이너가 있는 경우 중지합니다.
3. 배포 서버에서 저장소에 정의된 YAML 파일을 가져오기 위해 `git pull`을 수행합니다.
4. 원하는 Elasticsearch 구성 (Single, Master-slave, Raft-cluster)을 입력합니다.
5. 선택한 구성을 기반으로 필요한 Docker 컨테이너를 생성합니다.

## Apache Airflow 환경 자동화

Apache Airflow의 경우 소스 코드 버전 관리와 환경 설정 관리는 [참조 저장소](https://github.com/cucuridas/airflow_deploy)를 사용하여 파이프라인 내에서 수행됩니다.

![Airflow Jenkins Pipeline](doc_img/jenkins_pipeline_airflow.png)

1. GitHub 저장소를 Jenkins 서버로 다운로드하며 필요한 플러그인을 추가합니다.
2. 환경 설정 (Docker와 관련된)의 변경 사항이 있는지 확인합니다.
    - 변경 사항이 있는 경우, 단계 4 완료 후 Docker 이미지를 빌드합니다.
    - Docker 컨테이너를 다시 만듭니다 (구성 요소 및 워커 서버 모두에 대해 수행됩니다).
3. Airflow 구성 요소를 위한 서버에 저장소를 다운로드합니다.
4. Airflow 워커를 위한 서버에 저장소를 다운로드합니다.
