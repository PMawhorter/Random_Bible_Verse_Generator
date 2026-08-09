# Random Bible Verse Generator

A robust, academically accurate Android application that generates random Scripture references across various biblical canons and versification schemes. Built with modern Android standards using Jetpack Compose and Material 3.

## Overview

Most random verse generators are limited to a single translation or a standard Protestant canon. This app is designed for serious students of Scripture, providing support for multiple theological traditions and their unique organizational structures.

Whether you are looking for a random verse from the Hebrew Tanakh, a Catholic NABRE with its Greek Additions to Esther, or a Russian Orthodox Synodal Bible, this app ensures a **uniform distribution**—meaning every single verse in your selected canon has an equal mathematical probability of being chosen.

## Key Features

### Advanced Versification Schemes
Supports five distinct organizational structures to match your preferred Bible:
*   **KJV (Standard Protestant)**: NKJV, ESV, NIV, NASB, etc.
*   **NRSV**: Correctly handles the unique chapter/verse offsets of the New Revised Standard Version.
*   **Catholic (NABRE)**: Includes the full Deuterocanon and the unique lettered Greek Additions to Esther (A-F).
*   **Orthodox (Russian/Synodal)**: Includes the expanded Orthodox canon (1/2 Esdras, 3 Maccabees, Psalm 151) and LXX-style Psalm numbering.
*   **Hebrew (Tanakh/MT)**: Follows the traditional Jewish organization of the Masoretic Text.

### Smart Filtering
Filter your results by:
*   All Books
*   Old Testament
*   New Testament
*   Apocrypha / Deuterocanon (where applicable)

### Deep Link Integration
Once a verse is generated, open it instantly in your favorite Bible study tool. The app supports a tiered fallback system (Direct App → System Browser → BibleGateway) for:
*   **YouVersion**
*   **Logos Bible Study**
*   **Blue Letter Bible**
*   **ESV Bible (Crossway)**
*   **Olive Tree**
*   **AndBible**
*   **Life Bible (Tecarta)**
*   **Metanoia**

### Modern UI/UX
*   **Jetpack Compose & Material 3**: A clean, responsive interface that follows the latest Android design guidelines.
*   **Dynamic Animations**: Features a smooth "slot machine" roll animation when generating references.
*   **Persistent Settings**: Your preferred versification scheme is saved automatically.
*   **Dark Mode Support**: Fully adaptive theme for comfortable night reading.

## Technical Implementation

### Uniform Probability Algorithm
Unlike simpler apps that pick a random book, then a random chapter, then a random verse (which unfairly biases verses in shorter books), this app creates a flat "Verse Slot" map of the entire selected canon. A single random integer is generated across the total verse count, ensuring true mathematical uniformity.

### Catholic Esther Handling
Special logic was implemented to handle the **Greek Additions to Esther** (Chapters A, B, C, D, E, F). Since these are non-numeric, the app utilizes a custom `LabeledChapter` data structure and redirects these specific instances to BibleGateway's NABRE landing pages, as most standard mobile app schemes do not support non-numeric chapter navigation.

## Getting Started

### Prerequisites
*   Android device running API 24 (Nougat) or higher.
*   (Optional) One or more supported Bible apps installed for direct deep-linking.

### Installation
1.  Clone the repository:
    ```bash
    git clone https://github.com/PMawhorter/Random_Bible_Verse_Generator.git
    ```
2.  Open the project in **Android Studio (Ladybug or newer)**.
3.  Build and run the `app` module.

## License

This project is open-source. See the [LICENSE](LICENSE) file for details.

---
*Developed by Paul Mawhorter*
