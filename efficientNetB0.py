import random
import sys
import warnings
warnings.filterwarnings('ignore')

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import cv2
import os
import time
from tqdm import tqdm
from collections import defaultdict
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import classification_report, confusion_matrix, accuracy_score
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.models import Sequential, Model, load_model
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D, Dropout, BatchNormalization
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.utils import to_categorical
from tensorflow.keras.applications import EfficientNetB0
from tensorflow.keras.callbacks import EarlyStopping, ReduceLROnPlateau
from tensorflow.keras.regularizers import l2

IMG_SIZE = 224
MODEL_PATH = "effNetB0_painting_model.h5"

# Art style directories
style_dirs = {
    'AbstractExpressionism': 'C:/Users/deaka/OneDrive/Documents/Disszertacio/wikiart_concat/Abstract_Expressionism',
    'Baroque': 'C:/Users/deaka/OneDrive/Documents/Disszertacio/wikiart_concat/Baroque',
    'Cubism': 'C:/Users/deaka/OneDrive/Documents/Disszertacio/wikiart_concat/Cubism',
    'Expressionism': 'C:/Users/deaka/OneDrive/Documents/Disszertacio/wikiart_concat/Expressionism',
    'Fauvism': 'C:/Users/deaka/OneDrive/Documents/Disszertacio/wikiart_concat/Fauvism',
    'Impressionism': 'C:/Users/deaka/OneDrive/Documents/Disszertacio/Real_Imp_reduced/Impressionism',
    'Minimalism': 'C:/Users/deaka/OneDrive/Documents/Disszertacio/wikiart_concat/Minimalism',
    'NaiveArtPrimitivism': 'C:/Users/deaka/OneDrive/Documents/Disszertacio/wikiart_concat/Naive_Art_Primitivism',
    'PopArt': 'C:/Users/deaka/OneDrive/Documents/Disszertacio/wikiart_concat/Pop_Art',
    'Realism': 'C:/Users/deaka/OneDrive/Documents/Disszertacio/Real_Imp_reduced/Realism',
    'Renaissance': 'C:/Users/deaka/OneDrive/Documents/Disszertacio/wikiart_concat/Renaissance',
    'Rococo': 'C:/Users/deaka/OneDrive/Documents/Disszertacio/wikiart_concat/Rococo',
    'Romanticism': 'C:/Users/deaka/OneDrive/Documents/Disszertacio/wikiart_concat/Romanticism',
    'Symbolism': 'C:/Users/deaka/OneDrive/Documents/Disszertacio/wikiart_concat/Symbolism',
    # 'AI_Generated': 'C:/Users/deaka/OneDrive/Documents/Disszertacio/ai_gen'
}

def is_ascii(s):
    try:
        s.encode('ascii')
        return True
    except UnicodeEncodeError:
        return False

def load_data(style_dirs):
    X, Z = [], []
    
    print(f"Loading images from {len(style_dirs)} art styles...")
    print(f"Style directories: {list(style_dirs.keys())}")
    
    for style_name, DIR in style_dirs.items():
        print(f"\nLoading {style_name} images from: {DIR}")
        
        if not os.path.exists(DIR):
            print(f"Warning: Directory {DIR} does not exist!")
            continue
            
        files = os.listdir(DIR)
        print(f"Found {len(files)} files in directory")
        
        for img in tqdm(files, desc=f"Loading {style_name}"):
            if not is_ascii(img):
                continue
                
            try:
                path = os.path.join(DIR, img)
                image = cv2.imread(path)
                if image is None:
                    continue
                    
                image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
                image = cv2.resize(image, (IMG_SIZE, IMG_SIZE))
                X.append(image)
                Z.append(style_name)
                
            except Exception as e:
                print(f"Error loading {img}: {e}")
        
        print(f"Successfully loaded images for {style_name}")
                
    print(f"\nTotal images loaded: {len(X)}")
    print(f"Class distribution: {dict(zip(*np.unique(Z, return_counts=True)))}")
    
    return np.array(X), np.array(Z)

def build_model(num_classes, img_size=IMG_SIZE):
    base_model = EfficientNetB0(include_top=False, input_shape=(IMG_SIZE, IMG_SIZE, 3), weights='imagenet')
    base_model.trainable = True
    
    x = base_model.output
    x = GlobalAveragePooling2D()(x)
    # x = Dense(256, activation='relu')(x)
    # x = Dropout(0.5)(x)
    x = Dense(256, activation='relu')(x)
    # x = Dense(512, activation='relu', kernel_regularizer=l2(0.01))(x)
    x = Dropout(0.5)(x)
    # x = Dropout(0.6)(x)
    # x = BatchNormalization()(x)
    # x = Dense(256, activation='relu', kernel_regularizer=l2(0.01))(x)
    # x = Dropout(0.5)(x)
    predictions = Dense(num_classes, activation='softmax')(x)
    
    model = Model(inputs=base_model.input, outputs=predictions)
    model.compile(optimizer=Adam(learning_rate=1e-5), 
                 loss='categorical_crossentropy', 
                 metrics=['accuracy'])
    
    return model

def plot_training_history(history):
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(15, 5))
    
    # Plot loss
    ax1.plot(history.history['loss'], label='Training Loss')
    ax1.plot(history.history['val_loss'], label='Validation Loss')
    ax1.set_title('Model Loss')
    ax1.set_xlabel('Epochs')
    ax1.set_ylabel('Loss')
    ax1.legend()
    ax1.grid(True)
    
    # Plot accuracy
    ax2.plot(history.history['accuracy'], label='Training Accuracy')
    ax2.plot(history.history['val_accuracy'], label='Validation Accuracy')
    ax2.set_title('Model Accuracy')
    ax2.set_xlabel('Epochs')
    ax2.set_ylabel('Accuracy')
    ax2.legend()
    ax2.grid(True)
    
    plt.tight_layout()
    plt.show()

def plot_confusion_matrix_heatmap(y_true, y_pred, class_names):
    cm = confusion_matrix(y_true, y_pred)
    
    plt.figure(figsize=(10, 8))
    sns.heatmap(cm, annot=True, fmt='d', cmap='Blues', 
                xticklabels=class_names, yticklabels=class_names,
                cbar_kws={'label': 'Count'})
    plt.title('Confusion Matrix Heatmap', fontsize=16)
    plt.xlabel('Predicted Style', fontsize=12)
    plt.ylabel('True Style', fontsize=12)
    plt.xticks(rotation=45)
    plt.yticks(rotation=0)
    plt.tight_layout()
    plt.show()
    
    return cm

def plot_confusion_matrix_with_images(x_test, y_true, y_pred, class_names):
    num_classes = len(class_names)
    
    # Create matrix to store first occurrence of each prediction type
    image_indices = np.full((num_classes, num_classes), -1, dtype=int)
    
    # Find first occurrence for each (true, predicted) pair
    for idx, (true_idx, pred_idx) in enumerate(zip(y_true, y_pred)):
        if image_indices[true_idx, pred_idx] == -1:
            image_indices[true_idx, pred_idx] = idx
    
    fig, axes = plt.subplots(num_classes, num_classes, 
                           figsize=(3*num_classes, 3*num_classes))
    
    if num_classes == 2:
        axes = axes.reshape(2, -1)
    
    for i in range(num_classes):
        for j in range(num_classes):
            ax = axes[i][j]
            ax.set_xticks([])
            ax.set_yticks([])
            
            idx = image_indices[i, j]
            
            if idx != -1:
                ax.imshow(x_test[idx])
                
                # Set border color (green for correct, red for incorrect)
                border_color = 'green' if i == j else 'red'
                border_width = 3
                
                for spine in ax.spines.values():
                    spine.set_edgecolor(border_color)
                    spine.set_linewidth(border_width)
                    spine.set_visible(True)
            else:
                ax.text(0.5, 0.5, 'No\nExample', 
                       ha='center', va='center', fontsize=10,
                       transform=ax.transAxes)
                ax.set_facecolor('lightgray')
            
            if i == 0:
                ax.set_title(f'Predicted: {class_names[j]}', fontsize=10, pad=10)
            if j == 0:
                ax.set_ylabel(f'True: {class_names[i]}', fontsize=10, rotation=90, labelpad=10)
    
    plt.tight_layout()
    plt.subplots_adjust(top=0.95)
    plt.show()

def plot_class_performance_comparison(report, class_names):
    metrics = ['precision', 'recall', 'f1-score']
    x = np.arange(len(class_names))
    width = 0.25
    
    fig, ax = plt.subplots(figsize=(12, 6))
    
    for i, metric in enumerate(metrics):
        values = [report[class_name][metric] if class_name in report else 0 
                 for class_name in class_names]
        ax.bar(x + i*width, values, width, label=metric.capitalize())
    
    ax.set_xlabel('Art Styles')
    ax.set_ylabel('Score')
    ax.set_title('Per-Class Performance Comparison')
    ax.set_xticks(x + width)
    ax.set_xticklabels(class_names, rotation=45)
    ax.legend()
    ax.grid(True, alpha=0.3)
    
    plt.tight_layout()
    plt.show()

def plot_prediction_confidence_distribution(confidences, correct_mask):
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(15, 5))
    
    ax1.hist(confidences, bins=20, alpha=0.7, color='blue', edgecolor='black')
    ax1.axvline(np.mean(confidences), color='red', linestyle='--', 
                label=f'Mean: {np.mean(confidences):.3f}')
    ax1.set_xlabel('Prediction Confidence')
    ax1.set_ylabel('Frequency')
    ax1.set_title('Overall Prediction Confidence Distribution')
    ax1.legend()
    ax1.grid(True, alpha=0.3)
    
    ax2.hist(confidences[correct_mask], bins=15, alpha=0.7, color='green', 
             label='Correct Predictions', edgecolor='black')
    ax2.hist(confidences[~correct_mask], bins=15, alpha=0.7, color='red', 
             label='Incorrect Predictions', edgecolor='black')
    ax2.set_xlabel('Prediction Confidence')
    ax2.set_ylabel('Frequency')
    ax2.set_title('Confidence Distribution: Correct vs Incorrect')
    ax2.legend()
    ax2.grid(True, alpha=0.3)
    
    plt.tight_layout()
    plt.show()

def evaluate_model(model, x_test, y_test, class_names, x_train=None, y_train=None):
    print("COMPREHENSIVE MODEL EVALUATION")
    print('\n')
    
    y_pred_proba = model.predict(x_test)
    y_pred = np.argmax(y_pred_proba, axis=1)
    y_true = np.argmax(y_test, axis=1)
    
    from sklearn.metrics import (precision_score, recall_score, f1_score, 
                                balanced_accuracy_score, cohen_kappa_score)
    
    accuracy = accuracy_score(y_true, y_pred)
    balanced_acc = balanced_accuracy_score(y_true, y_pred)
    macro_precision = precision_score(y_true, y_pred, average='macro')
    macro_recall = recall_score(y_true, y_pred, average='macro')
    macro_f1 = f1_score(y_true, y_pred, average='macro')
    weighted_f1 = f1_score(y_true, y_pred, average='weighted')
    kappa = cohen_kappa_score(y_true, y_pred)
    
    print("OVERALL PERFORMANCE METRICS:")
    print('\n')
    print(f"Test Accuracy:           {accuracy:.4f} ({accuracy*100:.2f}%)")
    print(f"Balanced Accuracy:       {balanced_acc:.4f} ({balanced_acc*100:.2f}%)")
    print(f"Macro-averaged Precision: {macro_precision:.4f}")
    print(f"Macro-averaged Recall:    {macro_recall:.4f}")
    print(f"Macro-averaged F1-Score:  {macro_f1:.4f}")
    print(f"Weighted F1-Score:        {weighted_f1:.4f}")
    print(f"Cohen's Kappa:           {kappa:.4f}")
    
    print("\nPER-CLASS PERFORMANCE:")
    print('\n')
    
    n_classes = len(np.unique(np.concatenate([y_true, y_pred])))
    actual_class_names = class_names[:n_classes] if len(class_names) >= n_classes else class_names
    
    report = classification_report(y_true, y_pred, target_names=actual_class_names, 
                                 output_dict=True, zero_division=0)
    
    print(f"{'Class':<15} {'Precision':<10} {'Recall':<10} {'F1-Score':<10} {'Support':<10}")
    print('\n')
    for class_name in actual_class_names:
        if class_name in report:
            p = report[class_name]['precision']
            r = report[class_name]['recall']
            f1 = report[class_name]['f1-score']
            support = int(report[class_name]['support'])
            print(f"{class_name:<15} {p:<10.4f} {r:<10.4f} {f1:<10.4f} {support:<10}")
    
    if x_train is not None and y_train is not None:
        print("\nTRAINING vs TEST PERFORMANCE:")
        print('\n')
        y_train_pred_proba = model.predict(x_train)
        y_train_pred = np.argmax(y_train_pred_proba, axis=1)
        y_train_true = np.argmax(y_train, axis=1)
        train_accuracy = accuracy_score(y_train_true, y_train_pred)
        
        print(f"Training Accuracy:  {train_accuracy:.4f} ({train_accuracy*100:.2f}%)")
        print(f"Test Accuracy:      {accuracy:.4f} ({accuracy*100:.2f}%)")
        print(f"Generalization Gap: {abs(train_accuracy - accuracy):.4f}")
        
        if train_accuracy - accuracy > 0.1:
            print("Possible overfitting detected (gap > 10%)")
        elif train_accuracy - accuracy < 0.02:
            print("Good generalization (gap < 2%)")
    
    print("\nCLASS DISTRIBUTION ANALYSIS:")
    print('\n')
    unique_true, counts_true = np.unique(y_true, return_counts=True)
    unique_pred, counts_pred = np.unique(y_pred, return_counts=True)
    
    print(f"{'Class':<15} {'True Count':<12} {'Pred Count':<12} {'Difference':<12}")
    print('\n')
    for i, class_name in enumerate(actual_class_names):
        true_count = counts_true[i] if i < len(counts_true) else 0
        pred_count = counts_pred[np.where(unique_pred == i)[0][0]] if i in unique_pred else 0
        diff = pred_count - true_count
        print(f"{class_name:<15} {true_count:<12} {pred_count:<12} {diff:+<12}")
    
    print("\nPREDICTION CONFIDENCE ANALYSIS:")
    print('\n')
    max_probs = np.max(y_pred_proba, axis=1)
    correct_mask = (y_pred == y_true)
    
    avg_conf_correct = np.mean(max_probs[correct_mask])
    avg_conf_incorrect = np.mean(max_probs[~correct_mask]) if np.sum(~correct_mask) > 0 else 0
    
    print(f"Average confidence (correct predictions):   {avg_conf_correct:.4f}")
    print(f"Average confidence (incorrect predictions): {avg_conf_incorrect:.4f}")
    print(f"Overall average confidence:                 {np.mean(max_probs):.4f}")
    
    low_conf_threshold = 0.6
    low_conf_mask = max_probs < low_conf_threshold
    low_conf_count = np.sum(low_conf_mask)
    print(f"Low confidence predictions (<{low_conf_threshold}):        {low_conf_count} ({low_conf_count/len(y_pred)*100:.1f}%)")
    
    cm = plot_confusion_matrix_heatmap(y_true, y_pred, actual_class_names)
    plot_confusion_matrix_with_images(x_test, y_true, y_pred, actual_class_names)
    
    # Plot additional visualizations
    plot_class_performance_comparison(report, actual_class_names)
    plot_prediction_confidence_distribution(max_probs, correct_mask)
    
    # Create summary dictionary for easy access
    evaluation_summary = {
        'accuracy': accuracy,
        'balanced_accuracy': balanced_acc,
        'macro_precision': macro_precision,
        'macro_recall': macro_recall,
        'macro_f1': macro_f1,
        'weighted_f1': weighted_f1,
        'cohen_kappa': kappa,
        'confusion_matrix': cm,
        'classification_report': report,
        'avg_confidence_correct': avg_conf_correct,
        'avg_confidence_incorrect': avg_conf_incorrect,
        'low_confidence_count': low_conf_count
    }
    
    print("EVALUATION COMPLETED")
    print('\n')
    
    return y_pred, y_true, evaluation_summary

def measure_inference_time(model, x_test, n_samples=10):
    print(f"\nMeasuring inference time on {n_samples} samples...")
    nr_samples = 10
    start_time = time.time()
    for i in range(nr_samples):
        _ = model.predict(np.expand_dims(x_test[i], axis=0), verbose=0)
    end_time = time.time()
    
    avg_time = (end_time - start_time) / n_samples
    print(f"Average inference time per image: {avg_time:.4f} seconds")
    
    return avg_time

def main():
    print("Starting Art Style Classification")
    print('\n')
    
    # Load and preprocess data
    print("Loading data...")
    X, Z = load_data(style_dirs)
    
    # Encode labels
    le = LabelEncoder()
    Y = le.fit_transform(Z)
    Y = to_categorical(Y, len(le.classes_))
    
    # Normalize images
    X = X.astype('float32') / 255.0
    
    print(f"Loaded {len(X)} images from {len(le.classes_)} styles")
    print(f"Actual classes found: {le.classes_}")
    print(f"Class distribution: {dict(zip(*np.unique(Z, return_counts=True)))}")
    
    # Ensure we have enough data
    if len(X) == 0:
        raise ValueError("No images were loaded! Check your directory paths.")
    if len(le.classes_) != len(style_dirs):
        print(f"Warning: Expected {len(style_dirs)} classes but found {len(le.classes_)}")
    
    # Split data
    x_train, x_test, y_train, y_test = train_test_split(
        X, Y, test_size=0.2, random_state=42, stratify=Z
    )
    
    print(f"Training set: {len(x_train)} images")
    print(f"Test set: {len(x_test)} images")
    
    # Data augmentation
    datagen = ImageDataGenerator(
        # rotation_range=15,
        zoom_range=0.1,
        width_shift_range=0.1,
        height_shift_range=0.1,
        # horizontal_flip=True,
        fill_mode='nearest'
    )
    datagen.fit(x_train)
    
    # Build model
    print("Building model...")
    model = build_model(len(le.classes_))  # Use actual number of classes
    print(f"Model built with {len(le.classes_)} output classes")
    
    # Callbacks
    early_stop = EarlyStopping(monitor='val_loss', patience=5, restore_best_weights=True)
    reduce_lr = ReduceLROnPlateau(monitor='val_accuracy', patience=3, factor=0.1, verbose=1)
    
    # Train model
    print("Training model...")
    history = model.fit(
        datagen.flow(x_train, y_train, batch_size=32),
        validation_data=(x_test, y_test),
        steps_per_epoch=len(x_train) // 32,
        epochs=30,
        callbacks=[
            early_stop, 
            reduce_lr],
        verbose=1
    )
    
    # Save model
    model.save(MODEL_PATH)
    print(f"Model saved to {MODEL_PATH}")
    
    # Plot training history
    plot_training_history(history)
    
    # Evaluate model
    class_names = le.classes_
    y_pred, y_true, evaluation_summary = evaluate_model(
        model, x_test, y_test, class_names, x_train, y_train
    )
    
    # Measure inference time
    measure_inference_time(model, x_test)
    
    print("\nPipeline completed successfully!")
    
    return model, history, le

if __name__ == "__main__":
    # Run the pipeline
    model, history, label_encoder = main()