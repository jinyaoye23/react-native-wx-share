# react-native-wx-share-module

## Getting started

`$ npm install react-native-wx-share-module --save`

### Mostly automatic installation

`$ react-native link react-native-wx-share-module`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-wx-share-module` and add `WxShareModule.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libWxShareModule.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.reactlibrary.WxShareModulePackage;` to the imports at the top of the file
  - Add `new WxShareModulePackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-wx-share-module'
  	project(':react-native-wx-share-module').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-wx-share-module/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-wx-share-module')
  	```


## Usage
```javascript
import WxShareModule from 'react-native-wx-share-module';

// TODO: What to do with the module?
WxShareModule;
```
