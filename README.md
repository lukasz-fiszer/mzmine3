## GSoC 2020 review

##### Automatic mass calibration support and High-resolution deisotoping

Most of the commits are in the branch at: https://github.com/lukasz-fiszer/mzmine3/tree/shift-testing-merge
and at the corresponding pull request: https://github.com/mzmine/mzmine3/pull/78


We have managed to create mass calibration support that includes more features and is more robust than we have initially planned. This includes adding more regression modes (KNN regression) or a choice between two error extraction modes. As always, there is still a lot of potential improvements that can be researched and added. On the code side, I have ideas for few very concrete updates that could benefit the module, this includes certain refactoring of few parts of the codebase and improving runtime performance of the module (for instance more efficient range search in standards list, this can be done for instance with a k-d tree). These are not a necessity for the module, but they are still a very valuable upgrades.

While building a more robust mass calibration module, I have not managed to build the deisotoping module on time, by the end of August. My intention is to create deisotoping support over the next month, so we can have a new module and MZmine can benefit from the addition.

Acknowledgments:
Thank you to the project mentors and the MZmine team for their support. Thank you to Tomas Pluskal, Albert Rivas Ubach, Robin Schmid, Daniel Petras and Steffen Heu for their time introducing me to details of mass spectrometry, sharing and discussing suggestions, testing and providing feedback on the module. 

GSoC project link: https://summerofcode.withgoogle.com/projects/#6529966893694976

---


![MZmine 3](logo/MZmine_logo_RGB.png)

[![Build Status](https://travis-ci.org/mzmine/mzmine3.svg?branch=master)](https://travis-ci.org/mzmine/mzmine3)

MZmine is an open-source software for mass-spectrometry data processing. The goals of the project is to provide a user-friendly, flexible and easily extendable software with a complete set of modules covering the entire MS data analysis workflow.

More information about the software can be found on the [MZmine](http://mzmine.github.io) website.


## License
MZmine is a free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either [version 2](http://www.gnu.org/licenses/gpl-2.0.html) of the License, or (at your option) any [later version](http://www.gnu.org/licenses/gpl.html).

MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.


## Development

### Tutorial

Please read our brief [tutorial](http://mzmine.github.io/development.html) on how to contribute new code to MZmine.

### Java version

MZmine development requires Java Development Kit (JDK) version 12 or newer (http://jdk.java.net).

### Building

To build the MZmine package from the sources, run the following command:

    ./gradlew

or

    gradlew.bat

The final MZmine distribution will be placed in build/MZmine-version-platform.zip

If you encounter any problems, please contact the developers:
https://github.com/mzmine/mzmine3/issues

### Code style

Since this is a collaborative project, please adhere to the following code formatting conventions:
* We use the Google Java Style Guide (https://github.com/google/styleguide)
* Please write JavaDoc comments as full sentences, starting with a capital letter and ending with a period. Brevity is preferred (e.g., "Calculates standard deviation" is preferred over "This method calculates and returns a standard deviation of given set of numbers").

