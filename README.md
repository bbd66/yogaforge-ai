# YogaForge AI

<p align="center">
  <img alt="Vue" src="https://img.shields.io/badge/Frontend-Vue%203%20%2B%20TypeScript-42b883?style=for-the-badge&logo=vue.js&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Backend-Spring%20Boot%203-6db33f?style=for-the-badge&logo=springboot&logoColor=white">
  <img alt="PaddleDetection" src="https://img.shields.io/badge/AI-PaddleDetection-0052d9?style=for-the-badge">
  <img alt="ChatGLM" src="https://img.shields.io/badge/LLM-ChatGLM2--6B-1f2937?style=for-the-badge">
</p>

<p align="center">
  An AI yoga coach system for mobile: combining computer vision, LLM reasoning, and highly reliable backend services.
</p>

---

## Project Snapshot

- Vision-based motion evaluation engine: built on PaddlePaddle for keypoint detection, pose scoring, and posture correction.
- Language coaching engine: fine-tuned ChatGLM2-6B served online to generate natural-language training feedback.
- Engineering-ready backend: scalable API architecture with concurrency control, timeout retries, and fault isolation.
- Deployment and integration capability: Dockerized delivery with stable multi-module, cross-language collaboration.

---

## My Role

1. Led end-to-end architecture and development of an AI yoga coach system integrating computer vision, LLMs, and backend services.
2. Implemented yoga pose recognition and scoring with PaddlePaddle, delivering correction suggestions and training analytics to mobile clients.
3. Built service-oriented APIs for Paddle models, fine-tuned ChatGLM2-6B, and exposed online inference endpoints.
4. Designed PostgreSQL schema and backend service architecture, completed Docker-based deployment, and drove cross-language integration.
5. Optimized inference concurrency and retry policies to improve system stability, availability, and service reliability.

---

## Project Structure

```text
.
├─ frontend/                         # Vue 3 + TypeScript + Vite frontend app
│  ├─ src/views/                     # Core product pages (training, analytics, settings, etc.)
│  ├─ src/stores/                    # Pinia state management
│  ├─ src/components/                # Shared UI components
│  └─ src/api/                       # Frontend API client layer
│
├─ backend/
│  └─ backend/
│     ├─ springboot-server/          # Core backend services (auth, sessions, analytics APIs)
│     ├─ PaddleDetection/            # Paddle-based pose recognition and scoring models
│     └─ ChatGLM2-6B/                # Fine-tuning and online inference services
│
└─ LARGE_FILES_NOTICE.md             # Large-file policy and retrieval instructions
```

---

## System Architecture

```text
Mobile App / Frontend
  │
  ▼
Spring Boot API Gateway
  ├─ Training orchestration (start session / score aggregation / history)
  ├─ Reliability strategies (concurrency control / retry / timeout / fallback)
  └─ PostgreSQL (users, training sessions, scores, analytics)
  │
  ├────────► PaddleDetection Service (pose recognition and scoring)
  └────────► ChatGLM2-6B Service (corrections and coaching feedback)
```

---

## Core Capabilities

1. Pose Recognition and Scoring
  Uses Paddle models to classify yoga poses and compute completion quality, stability, and key error points.
2. Intelligent Correction Suggestions
  Structures vision outputs and feeds them into an LLM to generate clear, actionable, layered guidance.
3. Online Inference Service Layer
  Wraps model capabilities as APIs with a unified calling protocol for real-time mobile integration.
4. Closed-Loop Training Analytics
  Stores sessions, score trajectories, and coaching outputs to build traceable progress analytics.
5. High-Availability Backend Protection
  Improves online stability through concurrency governance, retry logic, timeout guards, and observability.

---

## Engineering Highlights

- Model serviceization: converted offline model capabilities into reusable online APIs to reduce frontend integration complexity.
- Inference reliability: added retry and fallback mechanisms to reduce failures caused by transient network jitter.
- Concurrency optimization: tuned request queuing and resource scheduling for inference workloads to improve throughput.
- Cross-language integration: bridged Java backend services with Python AI services through a stable data contract.
- Containerized delivery: standardized runtime with Docker to minimize environment drift and deployment risk.

---

## Quick Start

### 1) Frontend

```bash
cd frontend
npm install
npm run dev
```

### 2) Spring Boot Server

```bash
cd backend/backend/springboot-server
mvn clean package
mvn spring-boot:run
```

### 3) AI Modules (Optional)

- ChatGLM2-6B: go to `backend/backend/ChatGLM2-6B/` and follow the local docs for fine-tuning and inference service startup.
- PaddleDetection: go to `backend/backend/PaddleDetection/` and follow the local docs for dependencies and model weights.
- For large files (weights/datasets), see `LARGE_FILES_NOTICE.md`.

---

## Typical API Workflow

1. The client uploads training images, video frames, or keypoint data.
2. The backend calls the pose service and receives pose classification plus detailed scoring.
3. The backend sends structured scoring outputs to the LLM service to generate correction guidance.
4. The backend aggregates scores and guidance, returns results to mobile, and persists data for historical analytics.

---

## Deleted Code Recovery Guide

If some files were deleted, use the following recovery paths.

### Scenario A: File was deleted after being committed

1. Find delete history:

```bash
git log --diff-filter=D --summary -- .
```

2. Restore a specific file into the current working tree:

```bash
git checkout <commit_id>^ -- <path/to/file>
```

3. Export a historical version only:

```bash
git show <commit_id>:<path/to/file> > recovered_file
```

### Scenario B: You want the repository state before a specific commit

```bash
git checkout <commit_id>
# Create a new branch after verification (recommended)
# git checkout -b recover-from-<commit_id>
```

### Scenario C: You deleted a file locally but have not committed yet

```bash
git restore <path/to/file>
```

### Scenario D: Recover directly from GitHub web UI

1. Open the repository Commits page.
2. Enter a commit before deletion.
3. Locate the target file and open Raw.
4. Save it locally and commit again.

---

## License


This project is intended for learning and practical development. For third-party models, datasets, and frameworks, follow their respective licenses.

Large Files Notice (GitHub Upload)
To ensure the repository can be pushed to GitHub successfully, the following large files/directories are excluded from version control:

Path	Type	Why It Is Not Uploaded	How to Obtain
backend/backend/ChatGLM2-6B/models/	LLM weights	Single files and total size are too large (far beyond GitHub limits)	Download ChatGLM2-6B weights from official model sources (recommended: HuggingFace or ModelScope)
backend/backend/ChatGLM2-6B/ptuning/glm310/	Local Python environment	Local environment folders are large and not reproducible	Recreate the virtual environment and install dependencies from requirements.txt
backend/backend/PaddleDetection/Yoga-82/	Training/inference dataset	Dataset is too large for source repository storage	Download from maintainer-provided package, Release assets, or team shared storage
backend/backend/PaddleDetection/pretrained/	Pretrained weights	Binary files are large	Download corresponding weights from official PaddleDetection model sources
backend/backend/PaddleDetection/output/	Training outputs	Generated artifacts should not be versioned	Generated automatically after local training
backend/backend/PaddleDetection/output_inference/	Inference outputs	Generated artifacts should not be versioned	Generated automatically after local inference
Recommended Setup Flow
Clone the repository first (without large files).
Install dependencies according to:
backend/backend/ChatGLM2-6B/requirements.txt
backend/backend/PaddleDetection/requirements.txt
Download and place model/data files into the expected directories:
ChatGLM weights: backend/backend/ChatGLM2-6B/models/chatglm2-6b/
Paddle pretrained weights: backend/backend/PaddleDetection/pretrained/
Yoga dataset: backend/backend/PaddleDetection/Yoga-82/
Start backend services and run verification.
Notes
This repository stores reproducible source code and configuration only, not oversized binary weights or datasets.
For team sharing of large files, use one of the following:
GitHub Release Assets
Cloud/object storage (OSS/S3)
Git LFS (be aware of quota and bandwidth limits)
