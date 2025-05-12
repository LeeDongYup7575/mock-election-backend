// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

contract VotingToken is ERC20, Ownable {
    mapping(address => bool) public hasReceivedInitialTokens;

    // 0.1 토큰 = 10^17 wei (UI에서는 1로 표시)
    uint256 public constant INITIAL_SUPPLY = 100000000000000000; // 0.1 토큰
    uint256 public constant VOTE_COST = 100000000000000000;     // 0.1 토큰

    constructor() ERC20("Voting Token", "VT") Ownable(msg.sender) {}

    function issueInitialTokens(address recipient) external onlyOwner returns (bool) {
        require(!hasReceivedInitialTokens[recipient], "Token already issued to this address");

        hasReceivedInitialTokens[recipient] = true;
        _mint(recipient, INITIAL_SUPPLY);

        return true;
    }

    function vote(uint256 candidateId) external returns (bool) {
        require(balanceOf(msg.sender) >= VOTE_COST, "Insufficient token balance");

        _burn(msg.sender, VOTE_COST);

        emit VoteCast(msg.sender, candidateId);

        return true;
    }

    event VoteCast(address indexed voter, uint256 candidateId);
}