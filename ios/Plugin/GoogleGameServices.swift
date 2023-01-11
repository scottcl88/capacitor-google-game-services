import Foundation

@objc public class GoogleGameServices: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
