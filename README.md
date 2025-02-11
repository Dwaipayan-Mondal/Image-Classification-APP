# NNAPI Image Classification App

## Overview
The **NNAPI Image Classification App** is an Android application that utilizes **TensorFlow Lite (TFLite)** with the **Android Neural Networks API (NNAPI)** for on-device image classification. This app enables users to select images from the gallery or capture photos using the camera, processes them using a pre-trained deep learning model, and displays classification results in real time.

## Features
- **Image Classification** using TensorFlow Lite.
- **Camera & Gallery Integration** for image selection.
- **Optimized Inference** with NNAPI for better performance on supported devices.
- **Custom View (HalfHeightImageView)** ensures dynamic image display.
- **Minimal UI** built for a seamless user experience.
- **Efficient Image Preprocessing** including normalization and resizing.

## Technologies Used
- **Android Studio (Kotlin)** for app development.
- **TensorFlow Lite (TFLite)** for on-device inference.
- **Neural Networks API (NNAPI)** for optimized model execution.
- **Jetpack Compose & XML** for UI.
- **MPAndroidChart** (optional) for visualizing data.

## Getting Started
### Prerequisites
- Android device with **API Level 27+ (Android 8.1 or later)**.
- Android Studio **Giraffe | 2022.3.1** or later.
- TensorFlow Lite Model (**.tflite** file).

### Installation
1. **Clone this repository**:
   ```sh
   git clone https://github.com/Dwaipayan-Mondal/Image-Classification-APP.git
   cd NNAPI-Image-Classifier
   ```
2. **Open the project in Android Studio**.
3. **Build & Run the app** on an emulator or a physical device.

## Usage
1. **Launch the app**.
2. Select an image from the **gallery** or capture using the **camera**.
3. The app preprocesses the image and runs inference using **TensorFlow Lite**.
4. View the **classification result** on-screen.

## Project Structure
```
NNAPI-Image-Classifier/
│-- app/
│   ├── src/main/java/com/example/imageclassifier/
│   │   ├── MainActivity.kt  # Handles image selection & inference
│   │   ├── ImageProcessor.kt  # Image preprocessing logic
│   │   ├── Classifier.kt  # TensorFlow Lite inference handling
│   │   ├── HalfHeightImageView.kt  # Custom UI component
│   ├── src/main/res/layout/
│   │   ├── activity_main.xml  # UI layout
│   ├── assets/
│   │   ├── model.tflite  # Pre-trained TensorFlow Lite model
│   ├── build.gradle  # Gradle dependencies
```

## Model Details
- **Model Type**: Pre-trained Image Classification Model
- **Input Size**: 224x224 (resized before inference)
- **Output**: Predicted label & confidence score
- **Optimization**: NNAPI Delegation for performance boost

## Dependencies
Add these dependencies to your `build.gradle (Module: app)`:
```gradle
dependencies {
    implementation 'org.tensorflow:tensorflow-lite:2.9.0'
    implementation 'org.tensorflow:tensorflow-lite-support:0.4.3'
    implementation 'org.tensorflow:tensorflow-lite-gpu:2.9.0'
}
```

## Performance Optimizations
- **Uses NNAPI Delegate** for execution on hardware accelerators.
- **Resizes input images** for faster inference.
- **Efficient memory management** for loading models.

## Roadmap
- [ ] Implement support for **custom models**.
- [ ] Add **real-time camera inference**.
- [ ] Improve UI/UX for better user experience.
- [ ] Implement **batch processing** for multiple image classification.

## Contributing
Contributions are welcome! Please follow these steps:
1. **Fork the repository**.
2. Create a **new branch** (`feature-new-feature`).
3. **Commit your changes**.
4. Open a **Pull Request**.


## Screenshots
![image1](https://github.com/user-attachments/assets/aa08ab8d-2102-4623-b43a-761a49808b6a) &nbsp;
![image2](https://github.com/user-attachments/assets/815005e1-3766-42fb-bcf0-908710d5910a)








