# Large Files Notice (GitHub Upload)

To ensure the repository can be pushed to GitHub successfully, the following large files/directories are excluded from version control:

| Path | Type | Why It Is Not Uploaded | How to Obtain |
|---|---|---|---|
| `backend/backend/ChatGLM2-6B/models/` | LLM weights | Single files and total size are too large (far beyond GitHub limits) | Download ChatGLM2-6B weights from official model sources (recommended: HuggingFace or ModelScope) |
| `backend/backend/ChatGLM2-6B/ptuning/glm310/` | Local Python environment | Local environment folders are large and not reproducible | Recreate the virtual environment and install dependencies from `requirements.txt` |
| `backend/backend/PaddleDetection/Yoga-82/` | Training/inference dataset | Dataset is too large for source repository storage | Download from maintainer-provided package, Release assets, or team shared storage |
| `backend/backend/PaddleDetection/pretrained/` | Pretrained weights | Binary files are large | Download corresponding weights from official PaddleDetection model sources |
| `backend/backend/PaddleDetection/output/` | Training outputs | Generated artifacts should not be versioned | Generated automatically after local training |
| `backend/backend/PaddleDetection/output_inference/` | Inference outputs | Generated artifacts should not be versioned | Generated automatically after local inference |

---

## Recommended Setup Flow

1. Clone the repository first (without large files).
2. Install dependencies according to:
   - `backend/backend/ChatGLM2-6B/requirements.txt`
   - `backend/backend/PaddleDetection/requirements.txt`
3. Download and place model/data files into the expected directories:
   - ChatGLM weights: `backend/backend/ChatGLM2-6B/models/chatglm2-6b/`
   - Paddle pretrained weights: `backend/backend/PaddleDetection/pretrained/`
   - Yoga dataset: `backend/backend/PaddleDetection/Yoga-82/`
4. Start backend services and run verification.

---

## Notes

- This repository stores reproducible source code and configuration only, not oversized binary weights or datasets.
- For team sharing of large files, use one of the following:
  - GitHub Release Assets
  - Cloud/object storage (OSS/S3)
  - Git LFS (be aware of quota and bandwidth limits)
