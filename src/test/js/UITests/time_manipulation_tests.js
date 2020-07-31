/**
 * Tests related to time manipulation functions
 *
 * Testing strategy: test timestamps can be converted to seconds and vice versa for 
 *                   times containing 0+ hours, 0+ minutes, 0+ seconds
 */


/** timestampToEpoch() tests */

describe('Test generic H:M:S', function () {
    const twoThirty = "2:30";

    it('verify timestamp converted to seconds', function() {
        
        expect(timestampToEpoch(twoThirty)).toEqual(9000);
    });
});